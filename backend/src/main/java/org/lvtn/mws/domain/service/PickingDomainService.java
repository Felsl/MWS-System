package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.*;
import org.lvtn.mws.domain.repository.*;

import java.util.ArrayList;
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
    private final IUserRepository userRepository;
    private final ISalesOrderRepository soRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final IIdGenerator idGenerator;

    public PickingDomainService(IPickingListRepository pickingRepository,
                                ISalesOrderRepository soRepository,
                                IInventoryBatchRepository batchRepository,
                                IStockMovementRepository stockMovementRepository,
                                IIdGenerator idGenerator,
                                IUserRepository iUserRepository) {
        this.pickingRepository       = pickingRepository;
        this.soRepository            = soRepository;
        this.batchRepository         = batchRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.userRepository          = iUserRepository;
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
                .pickNumber(generatePickNumber())
                .soId(soId)
                .status(PickingList.Status.PENDING)
                .build();

        for (SalesOrderDetail line : so.getDetails()) {
            int remaining = line.getQuantityOrdered();
            // [Bán theo NCC] chỉ gom lô của đúng NCC dòng đã chọn (null = mọi NCC).
            List<InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPickingBySupplier(
                            line.getProductId(), so.getWarehouseId(), line.getSupplierId());

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

        PickingListDetail detail = findDetail(pl, pickingListDetailId);

        // Xác định lô CẦN xuất theo id đã chốt bởi FEFO (khoá chính, duy nhất) và kiểm tra
        // công nhân quét đúng lô đó. Quét đúng = nhận trọn quantityToPick.
        InventoryBatch scanned = resolveExpectedBatchForScan(detail, scannedBatchNumber);
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

        PickingListDetail shortDetail = findDetail(pl, pickingListDetailId);
        // [Báo thiếu] Cho phép lô THAY THẾ khác lô FEFO chỉ định, miễn: tồn tại + ACTIVE +
        // đúng kho + đúng sản phẩm + đúng NCC (cùng NCC với lô cần xuất). FE gửi batchId đã chọn.
        InventoryBatch scanned = resolveSubstituteBatchForShort(shortDetail, scannedBatchNumber);
        String productId = shortDetail.getProductId();

        // (1) Xác nhận short-pick (kiểm tra lệch lô + biên độ số lượng nằm trong confirmShort)
        shortDetail.confirmShort(scanned.getId(), actualQty, confirmedBy);
        int shortfall = shortDetail.shortfall();

        // (2) Ghi thẻ kho ADJUST cho phần lệch
        StockMovement adjust = StockMovement.adjustmentForShortPick(
                idGenerator.generate(), productId, scanned.getWarehouseId(), scanned.getId(),
                scanned.getBinLocationId(),
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
            // [Bán theo NCC] bù thiếu vẫn trong phạm vi NCC của lô đang nhặt.
            List<InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPickingBySupplier(
                            productId, warehouseId, scanned.getSupplierId());

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
        List<PickingList> findAll =pickingRepository.findAll();
        List<PickingList> newList = new ArrayList<PickingList>();

        for(PickingList sl : findAll){
            String userName = userRepository.findById(sl.getAssignedTo()).map(User::getUsername).orElse("-");
            PickingList temp = new PickingList.Builder()
                    .id(sl.getId())
                    .pickNumber(sl.getPickNumber())
                    .soId(sl.getSoId())
                    .transferOrderId(sl.getTransferOrderId())
                    .assignedTo(userName)
                    .status(sl.getStatus())
                    .startedAt(sl.getStartedAt())
                    .completedAt(sl.getCompletedAt())
                    .build();
            newList.add(temp);
        }
        return newList;
    }

    public PickingList getById(String id) {
        return pickingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lệnh gom hàng: " + id));
    }

    /** Mã nghiệp vụ lệnh gom hàng — cùng cách sinh với transferNumber (prefix + id, đảm bảo duy nhất). */
    private String generatePickNumber() {
        String number;
        do {
            number = "PK-" + idGenerator.generate().toUpperCase();
        } while (pickingRepository.existsByPickNumber(number));
        return number;
    }

    /**
     * Xác định lô CẦN xuất cho một dòng nhặt và kiểm tra công nhân đã quét đúng lô.
     *
     * <p>QUAN TRỌNG: {@code batch_number} (mã lô/barcode) KHÔNG duy nhất toàn hệ thống —
     * cùng một mã lô có thể tồn tại ở nhiều kho/ô kệ (VD: "LOT-2026-07"). Vì vậy KHÔNG dùng
     * {@code findByBatchNumber} (sẽ ném NonUniqueResult khi trùng). Thay vào đó tra lô kỳ vọng
     * theo {@code detail.batchId} (khoá chính, luôn duy nhất — do FEFO chốt) rồi đối chiếu giá
     * trị công nhân quét. Chấp nhận quét bằng MÃ LÔ hoặc ID lô.
     */
    private InventoryBatch resolveExpectedBatchForScan(PickingListDetail detail, String scanned) {
        InventoryBatch expected = batchRepository.findById(detail.getBatchId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy lô hàng cần xuất (id=" + detail.getBatchId() + ")"));
        boolean matches = expected.getBatchNumber().equals(scanned)
                || expected.getId().equals(scanned);
        if (!matches) {
            throw new IllegalArgumentException(
                    "Sai mã lô hàng! Lô bạn vừa quét (" + scanned + ") không phải lô cận hạn nhất "
                    + "cần xuất (" + expected.getBatchNumber() + "). Vui lòng kiểm tra lại!");
        }
        return expected;
    }

    /**
     * [Báo thiếu] Tra lô THAY THẾ mà người lấy thực sự lấy (có thể khác lô FEFO chỉ định).
     * Hợp lệ khi: lô tồn tại + ACTIVE + đúng kho + đúng sản phẩm + đúng NCC (cùng NCC với lô cần xuất).
     * FE gửi batchId (duy nhất) nên tra theo id; vẫn chấp nhận quét bằng batch_number nếu khớp id.
     */
    private InventoryBatch resolveSubstituteBatchForShort(PickingListDetail detail, String scanned) {
        InventoryBatch expected = batchRepository.findById(detail.getBatchId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy lô cần xuất (id=" + detail.getBatchId() + ")"));
        InventoryBatch picked = batchRepository.findById(scanned).orElse(null);
        if (picked == null) {
            throw new IllegalArgumentException("Lô bạn chọn không tồn tại trong hệ thống");
        }
        if (picked.getStatus() != InventoryBatch.Status.ACTIVE) {
            throw new IllegalArgumentException("Lô " + picked.getBatchNumber() + " không ở trạng thái ACTIVE");
        }
        if (!java.util.Objects.equals(picked.getWarehouseId(), expected.getWarehouseId())) {
            throw new IllegalArgumentException("Lô thay thế phải ở đúng kho của lệnh nhặt");
        }
        if (!java.util.Objects.equals(picked.getProductId(), detail.getProductId())) {
            throw new IllegalArgumentException("Lô thay thế phải cùng sản phẩm");
        }
        if (!java.util.Objects.equals(picked.getSupplierId(), expected.getSupplierId())) {
            throw new IllegalArgumentException("Lô thay thế phải cùng nhà cung cấp với lô cần xuất");
        }
        return picked;
    }

    /**
     * [Báo thiếu] Danh sách lô ACTIVE (chưa hết hạn) người lấy có thể chọn cho một dòng nhặt:
     * cùng sản phẩm + NCC + kho với lô FEFO chỉ định.
     */
    public java.util.List<InventoryBatch> candidateBatchesForShort(String pickingListDetailId) {
        PickingList pl = pickingRepository.findByDetailId(pickingListDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng nhặt: " + pickingListDetailId));
        PickingListDetail detail = findDetail(pl, pickingListDetailId);
        InventoryBatch expected = batchRepository.findById(detail.getBatchId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy lô cần xuất (id=" + detail.getBatchId() + ")"));
        return batchRepository.findActiveBatchesForPickingBySupplier(
                detail.getProductId(), expected.getWarehouseId(), expected.getSupplierId());
    }

    private PickingListDetail findDetail(PickingList pl, String detailId) {
        return pl.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Dòng nhặt không thuộc lệnh này"));
    }
}