package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.InsufficientStockException;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.domain.repository.ITransferOrderRepository;

import java.util.List;

/**
 * Sinh lệnh gom hàng (picking list) cho ĐIỀU CHUYỂN — tách riêng khỏi luồng đơn xuất (SO).
 *
 * <p>Quy tắc chọn lô:
 * <ul>
 *   <li>Dòng CÓ {@code designatedBatchId} (người lập/duyệt chỉ định): tạo dòng nhặt khoá đúng lô đó
 *       ({@code requiredBatchId} = lô chỉ định) — picker phải quét đúng lô này.</li>
 *   <li>Dòng KHÔNG chỉ định: gợi ý theo FEFO ({@code requiredBatchId} = null) — picker được quét bất kỳ
 *       lô ACTIVE còn tồn của đúng sản phẩm tại kho nguồn (kiểm tra ở bước quét).</li>
 * </ul>
 */
public class TransferPickingDomainService {

    private final IPickingListRepository pickingRepository;
    private final IInventoryBatchRepository batchRepository;
    private final ITransferOrderRepository transferRepository;
    private final IIdGenerator idGenerator;

    public TransferPickingDomainService(IPickingListRepository pickingRepository,
                                        IInventoryBatchRepository batchRepository,
                                        ITransferOrderRepository transferRepository,
                                        IIdGenerator idGenerator) {
        this.pickingRepository = pickingRepository;
        this.batchRepository = batchRepository;
        this.transferRepository = transferRepository;
        this.idGenerator = idGenerator;
    }

    public PickingList generateForTransfer(String transferOrderId) {
        TransferOrder transfer = transferRepository.findById(transferOrderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy phiếu điều chuyển: " + transferOrderId));
        if (pickingRepository.findByTransferOrderId(transferOrderId).isPresent()) {
            throw new IllegalStateException("Phiếu điều chuyển này đã có lệnh gom hàng");
        }

        String fromWh = transfer.getFromWarehouseId();

        PickingList pickingList = new PickingList.Builder()
                .id(idGenerator.generate())
                .pickNumber(generatePickNumber())
                .transferOrderId(transferOrderId)
                .status(PickingList.Status.PENDING)
                .build();

        for (TransferOrderDetail line : transfer.getDetails()) {
            String mode = effectiveBatchMode(line);
            switch (mode) {
                case "DESIGNATED" -> addDesignatedLine(pickingList, line, fromWh);
                case "FEFO"       -> addFefoLines(pickingList, line, fromWh, true);   // FEFO gán lô CỨNG (khoá)
                default            -> addFefoLines(pickingList, line, fromWh, false); // ANY: gợi ý FEFO, quét bất kỳ (đúng NCC)
            }
        }

        PickingList saved = pickingRepository.save(pickingList);
        transfer.markPicking();               // APPROVED -> PICKING
        transferRepository.save(transfer);
        return saved;
    }

    /** Mã nghiệp vụ lệnh gom hàng — cùng cách sinh với transferNumber (prefix + id, đảm bảo duy nhất). */
    private String generatePickNumber() {
        String number;
        do {
            number = "PK-" + idGenerator.generate().toUpperCase();
        } while (pickingRepository.existsByPickNumber(number));
        return number;
    }

    /** Dòng chỉ định: khoá đúng lô người lập/duyệt đã chọn. */
    private void addDesignatedLine(PickingList pickingList, TransferOrderDetail line, String fromWh) {
        InventoryBatch b = batchRepository.findById(line.getDesignatedBatchId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lô chỉ định: " + line.getDesignatedBatchId()));
        if (!b.isAvailable()
                || !fromWh.equals(b.getWarehouseId())
                || !line.getProductId().equals(b.getProductId())) {
            throw new IllegalStateException(
                    "Lô chỉ định không hợp lệ (phải ACTIVE, cùng kho nguồn và đúng sản phẩm): "
                            + line.getDesignatedBatchId());
        }
        if (b.getQuantity() < line.getQuantity()) {
            throw new InsufficientStockException(
                    "Lô chỉ định " + b.getBatchNumber() + " không đủ tồn để gom ("
                            + b.getQuantity() + "/" + line.getQuantity() + ")");
        }
        pickingList.addDetail(new PickingListDetail.Builder()
                .id(idGenerator.generate())
                .pickingListId(pickingList.getId())
                .productId(line.getProductId())
                .batchId(b.getId())              // gợi ý = chính lô chỉ định
                .requiredBatchId(b.getId())      // KHOÁ: phải quét đúng lô này
                .binLocationId(b.getBinLocationId())
                .quantityToPick(line.getQuantity())
                .build());
    }

    /** Chế độ lô hiệu dụng của dòng (tương thích ngược: dòng cũ suy theo designatedBatchId). */
    private String effectiveBatchMode(TransferOrderDetail line) {
        String m = line.getBatchMode();
        if (m != null && !m.isBlank()) return m;
        return (line.getDesignatedBatchId() != null && !line.getDesignatedBatchId().isBlank())
                ? "DESIGNATED" : "ANY";
    }

    /**
     * Dòng FEFO/ANY: gom theo FEFO trong phạm vi NCC của dòng (null = mọi NCC).
     * lock=true (FEFO): khoá requiredBatchId = phải quét đúng lô FEFO đã gán.
     * lock=false (ANY): requiredBatchId=null, picker quét lô bất kỳ nhưng phải ĐÚNG NCC (chặn ở resolveScannedBatch).
     */
    private void addFefoLines(PickingList pickingList, TransferOrderDetail line, String fromWh, boolean lock) {
        int remaining = line.getQuantity();
        for (InventoryBatch batch : batchRepository.findActiveBatchesForPickingBySupplier(
                line.getProductId(), fromWh, line.getSupplierId())) {
            if (remaining <= 0) break;
            int available = batch.getQuantity();
            if (available <= 0) continue;
            int take = Math.min(remaining, available);
            pickingList.addDetail(new PickingListDetail.Builder()
                    .id(idGenerator.generate())
                    .pickingListId(pickingList.getId())
                    .productId(line.getProductId())
                    .batchId(batch.getId())                       // gợi ý FEFO
                    .requiredBatchId(lock ? batch.getId() : null) // FEFO: khoá; ANY: tự do
                    .binLocationId(batch.getBinLocationId())
                    .quantityToPick(take)
                    .build());
            remaining -= take;
        }
        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Không đủ lô ACTIVE để gom cho sản phẩm " + line.getProductId()
                            + " (còn thiếu " + remaining + ")");
        }
    }

    /**
     * Xác nhận quét lô cho một dòng gom hàng ĐIỀU CHUYỂN.
     *
     * <p>Resolve lô đã quét trong ĐÚNG kho nguồn (tránh trùng mã lô toàn hệ thống): chỉ xét lô của
     * đúng sản phẩm tại kho nguồn, ACTIVE và còn tồn. Nếu dòng có {@code requiredBatchId} (lô chỉ định)
     * thì bắt buộc khớp đúng lô đó; nếu không, nhận bất kỳ lô hợp lệ (ưu tiên đúng ô kệ gợi ý).
     * Ghi {@code actualBatchId} = lô thực lấy. KHÔNG trừ tồn ở đây — trừ tồn thực hiện lúc dispatch.
     */
    public PickingList confirmScanForTransfer(String pickingListDetailId,
                                              String scannedBatchNumberOrId,
                                              String confirmedBy) {
        PickingList pl = pickingRepository.findByDetailId(pickingListDetailId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy dòng nhặt: " + pickingListDetailId));
        if (pl.getTransferOrderId() == null) {
            throw new IllegalStateException("Dòng nhặt này không thuộc lệnh gom điều chuyển");
        }
        TransferOrder transfer = transferRepository.findById(pl.getTransferOrderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy phiếu điều chuyển: " + pl.getTransferOrderId()));
        String fromWh = transfer.getFromWarehouseId();

        PickingListDetail detail = findDetail(pl, pickingListDetailId);
        InventoryBatch batch = resolveScannedBatch(detail, scannedBatchNumberOrId, fromWh);

        if (pl.getStatus() == PickingList.Status.PENDING) {
            pl.assign(confirmedBy);   // PENDING -> PICKING (bắt đầu gom)
        }
        detail.confirmPicked(batch.getId(), confirmedBy);

        if (pl.getDetails().stream().allMatch(PickingListDetail::isConfirmed)) {
            pl.complete();            // PICKING -> COMPLETED
        }
        return pickingRepository.save(pl);
    }

    private InventoryBatch resolveScannedBatch(PickingListDetail detail, String scanned, String fromWh) {
        List<InventoryBatch> candidates = batchRepository
                .findByProductIdAndWarehouseId(detail.getProductId(), fromWh).stream()
                .filter(InventoryBatch::isAvailable) // ACTIVE + còn tồn
                .filter(b -> scanned.equals(b.getBatchNumber()) || scanned.equals(b.getId()))
                .toList();

        if (detail.getRequiredBatchId() != null) {
            InventoryBatch req = candidates.stream()
                    .filter(b -> b.getId().equals(detail.getRequiredBatchId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Sai lô! Dòng này bắt buộc quét đúng lô đã chỉ định."));
            ensureEnough(req, detail);
            return req;
        }

        // Gợi ý FEFO (mode ANY): nhận bất kỳ lô hợp lệ nhưng phải ĐÚNG NCC của lô gợi ý.
        String requiredSupplier = detail.getBatchId() == null ? null
                : batchRepository.findById(detail.getBatchId()).map(InventoryBatch::getSupplierId).orElse(null);
        InventoryBatch chosen = candidates.stream()
                .filter(b -> requiredSupplier == null
                        || java.util.Objects.equals(requiredSupplier, b.getSupplierId()))
                .filter(b -> detail.getBinLocationId() != null
                        && detail.getBinLocationId().equals(b.getBinLocationId()))
                .findFirst()
                .orElse(candidates.stream()
                        .filter(b -> requiredSupplier == null
                                || java.util.Objects.equals(requiredSupplier, b.getSupplierId()))
                        .findFirst().orElse(null));
        if (chosen == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy lô ACTIVE còn tồn khớp '" + scanned
                            + "' đúng nhà cung cấp cho sản phẩm này tại kho nguồn.");
        }
        ensureEnough(chosen, detail);
        return chosen;
    }

    private void ensureEnough(InventoryBatch b, PickingListDetail detail) {
        if (b.getQuantity() < detail.getQuantityToPick()) {
            throw new InsufficientStockException(
                    "Lô " + b.getBatchNumber() + " không đủ tồn để nhặt ("
                            + b.getQuantity() + "/" + detail.getQuantityToPick() + ")");
        }
    }

    private PickingListDetail findDetail(PickingList pl, String detailId) {
        return pl.getDetails().stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Dòng nhặt không thuộc lệnh này"));
    }
}
