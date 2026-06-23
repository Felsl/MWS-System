package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.InsufficientStockException;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.domain.repository.IStockMovementRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nghiệp vụ Lệnh gom hàng + giải thuật FEFO. Thuần Java.
 *
 * Giả định API có sẵn trên model InventoryBatch (module Giai đoạn 2):
 *   - String getId()
 *   - String getBinLocationId()
 *   - int    getQuantity()
 * và IInventoryBatchRepository.findActiveBatchesForPicking(...) ĐÃ sort theo FEFO
 * (expiry_date ASC, created_at ASC, status = ACTIVE).
 */
public class PickingDomainService {

    private final IPickingListRepository pickingRepository;
    private final ISalesOrderRepository soRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final IIdGenerator idGenerator;

    public PickingDomainService(IPickingListRepository pickingRepository,
                                ISalesOrderRepository soRepository,
                                IInventoryBatchRepository batchRepository,
                                IStockMovementRepository stockMovementRepository,
                                IIdGenerator idGenerator) {
        this.pickingRepository       = pickingRepository;
        this.soRepository            = soRepository;
        this.batchRepository         = batchRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.idGenerator             = idGenerator;
    }

    /**
     * FEFO Allocation Engine: từ một SO đang ALLOCATED, sinh picking_list + chi tiết nhặt,
     * sau đó nâng cấp SO sang PICKING.
     */
    public PickingList generateForSalesOrder(String soId) {
        SalesOrder so = soRepository.findById(soId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn bán hàng: " + soId));
        if (so.getStatus() != SalesOrder.Status.ALLOCATED) {
            throw new IllegalStateException("Chỉ tạo lệnh gom hàng từ đơn ở trạng thái ALLOCATED");
        }
        if (pickingRepository.findBySoId(soId).isPresent()) {
            throw new IllegalStateException("Đơn này đã có lệnh gom hàng");
        }

        PickingList pickingList = new PickingList.Builder()
                .id(idGenerator.generate())
                .soId(soId)
                .status(PickingList.Status.PENDING)
                .build();

        for (SalesOrderDetail line : so.getDetails()) {
            int remaining = line.getQuantityOrdered();
            List<InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPicking(line.getProductId(), so.getWarehouseId());

            for (InventoryBatch batch : batches) {
                if (remaining <= 0) break;
                int available = batch.getQuantity();
                if (available <= 0) continue;
                int take = Math.min(remaining, available);

                PickingListDetail detail = new PickingListDetail.Builder()
                        .id(idGenerator.generate())
                        .pickingListId(pickingList.getId())
                        .productId(line.getProductId())
                        .batchId(batch.getId())             // lô đề xuất tối ưu (FEFO)
                        .binLocationId(batch.getBinLocationId())
                        .quantityToPick(take)
                        .build();
                pickingList.addDetail(detail);
                remaining -= take;
            }

            if (remaining > 0) {
                throw new InsufficientStockException(
                        "Không đủ lô hàng ACTIVE để gom cho sản phẩm " + line.getProductId()
                                + " (còn thiếu " + remaining + ")");
            }
        }

        PickingList saved = pickingRepository.save(pickingList);
        so.markPicking();
        soRepository.save(so);
        return saved;
    }

    /** Công nhân nhận lệnh: PENDING -> PICKING. */
    public PickingList assign(String pickingListId, String userId) {
        PickingList pl = getById(pickingListId);
        pl.assign(userId);
        return pickingRepository.save(pl);
    }

    /**
     * Đối soát quét mã vạch (Double-Check Verification).
     * scannedBatchNumber = chuỗi mã vạch (batch_number) công nhân quét được.
     */
    public PickingList confirmScan(String pickingListDetailId,
                                   String scannedBatchNumber,
                                   String confirmedBy) {
        PickingList pl = pickingRepository.findByDetailId(pickingListDetailId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy dòng nhặt: " + pickingListDetailId));

        InventoryBatch scanned = batchRepository.findByBatchNumber(scannedBatchNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lô hàng theo mã vạch: " + scannedBatchNumber));

        PickingListDetail detail = findDetail(pl, pickingListDetailId);

        // Quét đúng lô = nhận trọn quantityToPick; lệch FEFO -> ném "Sai mã lô hàng!"
        detail.confirmScan(scanned.getId(), confirmedBy);
        return pickingRepository.save(pl);
    }

    /**
     * Báo thiếu hàng thực tế (short-pick) và TỰ BÙ phần thiếu từ lô FEFO kế tiếp.
     *
     * Quy trình:
     *  1) Xác nhận dòng hiện tại với số thực lấy (actualQty < quantityToPick).
     *  2) Ghi 1 thẻ kho ADJUST (-shortfall) trên lô thiếu, note = reason (truy vết QC/kiểm kê).
     *  3) Quét các lô ACTIVE còn lại theo FEFO (trừ phần đã phân bổ cho lệnh này) để tạo
     *     dòng nhặt MỚI (chưa xác nhận) bù đúng phần thiếu. Công nhân sẽ đi nhặt tiếp.
     *  4) Nếu không còn lô nào để bù -> ném InsufficientStockException (rollback toàn bộ).
     *
     * Lưu ý kế toán: thẻ kho ADJUST ghi nhận phần lệch để bộ phận kiểm kê chỉnh tồn vật lý;
     * tại bước này KHÔNG sửa trực tiếp inventory/inventory_batches (khấu trừ vẫn ở bước xuất hàng).
     */
    public PickingList reportShortPick(String pickingListDetailId,
                                       String scannedBatchNumber,
                                       int actualQty,
                                       String reason,
                                       String confirmedBy) {
        PickingList pl = pickingRepository.findByDetailId(pickingListDetailId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy dòng nhặt: " + pickingListDetailId));

        InventoryBatch scanned = batchRepository.findByBatchNumber(scannedBatchNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lô hàng theo mã vạch: " + scannedBatchNumber));

        PickingListDetail shortDetail = findDetail(pl, pickingListDetailId);
        String productId = shortDetail.getProductId();

        // (1) Xác nhận short-pick (kiểm tra lệch lô + biên độ số lượng nằm trong confirmShort)
        shortDetail.confirmShort(scanned.getId(), actualQty, confirmedBy);
        int shortfall = shortDetail.shortfall();

        // (2) Ghi thẻ kho ADJUST cho phần lệch
        StockMovement adjust = StockMovement.adjustmentForShortPick(
                idGenerator.generate(), productId, scanned.getWarehouseId(), scanned.getId(),
                shortfall, scanned.getQuantity(), pl.getId(), reason, confirmedBy);
        stockMovementRepository.save(adjust);

        // (3) Tự bù phần thiếu từ lô FEFO kế tiếp
        if (shortfall > 0) {
            SalesOrder so = soRepository.findById(pl.getSoId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy đơn của lệnh gom: " + pl.getSoId()));
            String warehouseId = so.getWarehouseId();

            // Tổng số đã phân bổ cho lệnh này theo từng lô (gồm cả lô vừa thiếu -> tự loại trừ)
            Map<String, Integer> allocatedByBatch = new HashMap<>();
            for (PickingListDetail d : pl.getDetails()) {
                if (!d.getProductId().equals(productId)) continue;
                allocatedByBatch.merge(d.getBatchId(), d.getQuantityToPick(), Integer::sum);
            }

            int remaining = shortfall;
            List<InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPicking(productId, warehouseId);

            for (InventoryBatch batch : batches) {
                if (remaining <= 0) break;
                int allocated = allocatedByBatch.getOrDefault(batch.getId(), 0);
                int availableInBatch = batch.getQuantity() - allocated; // phần còn trống của lô
                if (availableInBatch <= 0) continue;                    // lô đã dùng hết phần khả dụng
                int take = Math.min(remaining, availableInBatch);

                PickingListDetail compensating = new PickingListDetail.Builder()
                        .id(idGenerator.generate())
                        .pickingListId(pl.getId())
                        .productId(productId)
                        .batchId(batch.getId())
                        .binLocationId(batch.getBinLocationId())
                        .quantityToPick(take)
                        .build();
                pl.addDetail(compensating);
                allocatedByBatch.merge(batch.getId(), take, Integer::sum);
                remaining -= take;
            }

            if (remaining > 0) {
                throw new InsufficientStockException(
                        "Không còn lô khác để bù " + remaining + " đơn vị thiếu cho sản phẩm " + productId);
            }
        }

        return pickingRepository.save(pl);
    }

    /** Hoàn tất lệnh: yêu cầu mọi dòng đã đối soát. */
    public PickingList complete(String pickingListId) {
        PickingList pl = getById(pickingListId);
        pl.complete();
        return pickingRepository.save(pl);
    }

    /** Danh sách toàn bộ lệnh gom hàng (đọc). */
    public List<PickingList> findAll() {
        return pickingRepository.findAll();
    }

    public PickingList getById(String id) {
        return pickingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lệnh gom hàng: " + id));
    }

    private PickingListDetail findDetail(PickingList pl, String detailId) {
        return pl.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Dòng nhặt không thuộc lệnh này"));
    }
}