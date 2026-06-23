package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.domain.model.GoodsReceiptLineCommand;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IGoodsReceiptDetailRepository;
import org.lvtn.mws.domain.repository.IGoodsReceiptRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IPurchaseOrderDetailRepository;
import org.lvtn.mws.domain.repository.IPurchaseOrderRepository;
import org.lvtn.mws.domain.repository.IStockMovementRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Domain service for the inbound (goods-receipt) flow. Pure Java — no framework imports.
 * The transaction & optimistic-lock retry boundaries are applied in the UseCase layer.
 *
 * Reuses the Stage-2 {@link InventoryDomainService} and inventory repositories instead
 * of re-implementing inventory / inventory_batches.
 */
public class GoodsReceiptDomainService {

    private final IGoodsReceiptRepository grnRepository;
    private final IGoodsReceiptDetailRepository grnDetailRepository;
    private final IPurchaseOrderRepository poRepository;
    private final IPurchaseOrderDetailRepository poDetailRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final InventoryDomainService inventoryDomainService;
    private final IInventoryRepository inventoryRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IIdGenerator idGenerator;

    public GoodsReceiptDomainService(IGoodsReceiptRepository grnRepository,
                                     IGoodsReceiptDetailRepository grnDetailRepository,
                                     IPurchaseOrderRepository poRepository,
                                     IPurchaseOrderDetailRepository poDetailRepository,
                                     IStockMovementRepository stockMovementRepository,
                                     InventoryDomainService inventoryDomainService,
                                     IInventoryRepository inventoryRepository,
                                     IInventoryBatchRepository batchRepository,
                                     IIdGenerator idGenerator) {
        this.grnRepository           = grnRepository;
        this.grnDetailRepository     = grnDetailRepository;
        this.poRepository            = poRepository;
        this.poDetailRepository      = poDetailRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryDomainService  = inventoryDomainService;
        this.inventoryRepository     = inventoryRepository;
        this.batchRepository         = batchRepository;
        this.idGenerator             = idGenerator;
    }

    // ── Read ───────────────────────────────────────────────────────────────

    public GoodsReceipt findById(String id) {
        return grnRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập: " + id));
    }

    public List<GoodsReceiptDetail> findDetails(String grnId) {
        return grnDetailRepository.findByGrnId(grnId);
    }

    // ── 1. Create goods receipt (PENDING) + over-receiving guard ─────────────

    /**
     * Creates a PENDING goods receipt. When the source PO is set, the PO must be
     * APPROVED, and every line is checked against the over-receiving rule:
     * quantity_coming + quantity_received <= quantity_ordered.
     */
    public GoodsReceipt create(String poId, String warehouseId, String receivedBy,
                               String note, List<GoodsReceiptLineCommand> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một dòng hàng");
        }

        // Guard: only APPROVED purchase orders may back a receipt.
        if (poId != null && !poId.isBlank()) {
            PurchaseOrder po = poRepository.findById(poId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mua: " + poId));
            if (!po.canBeReceived()) {
                throw new IllegalStateException(
                        "Đơn mua " + po.getPoNumber() + " chưa được duyệt (APPROVED), không thể làm phiếu nhập");
            }
        }

        // Over-receiving protection per line.
        for (GoodsReceiptLineCommand line : lines) {
            if (line.getPoDetailId() != null && !line.getPoDetailId().isBlank()) {
                PurchaseOrderDetail pod = poDetailRepository.findById(line.getPoDetailId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy dòng PO: " + line.getPoDetailId()));
                if (pod.wouldExceedOnReceiving(line.getQuantity())) {
                    throw new IllegalArgumentException(
                            "Lỗi: Số lượng nhập vượt quá số lượng đặt mua trong đơn PO gốc");
                }
            }
        }

        String grnId = idGenerator.generate();
        GoodsReceipt grn = GoodsReceipt.builder()
                .id(grnId)
                .grnNumber(generateGrnNumber())
                .poId((poId != null && poId.isBlank()) ? null : poId)
                .warehouseId(warehouseId)
                .status(GoodsReceipt.Status.PENDING)
                .receivedBy(receivedBy)
                .note(note)
                .build();
        GoodsReceipt saved = grnRepository.save(grn);

        List<GoodsReceiptDetail> details = new ArrayList<>();
        for (GoodsReceiptLineCommand line : lines) {
            details.add(GoodsReceiptDetail.builder()
                    .id(idGenerator.generate())
                    .grnId(grnId)
                    .productId(line.getProductId())
                    .poDetailId(line.getPoDetailId())
                    .quantity(line.getQuantity())
                    .batchNumber(line.getBatchNumber())
                    .expiryDate(line.getExpiryDate())
                    .binLocationId(line.getBinLocationId())
                    .build());
        }
        grnDetailRepository.saveAll(details);
        return saved;
    }

    // ── 2. Complete goods receipt — the inbound heart ────────────────────────

    /**
     * Completes a goods receipt. The whole chain must run inside a single
     * transaction (applied by the UseCase via @Transactional):
     *   PO cumulative update -> inventory total -> inventory_batches -> stock_movements -> close PO.
     */
    public GoodsReceipt complete(String grnId) {
        GoodsReceipt grn = findById(grnId);
        grn.complete(); // PENDING -> COMPLETED (guards duplicate completion)

        List<GoodsReceiptDetail> details = grnDetailRepository.findByGrnId(grnId);

        for (GoodsReceiptDetail detail : details) {
            // 1) Cumulative progress on the source PO line.
            if (grn.isLinkedToPurchaseOrder() && detail.isLinkedToPoDetail()) {
                PurchaseOrderDetail pod = poDetailRepository.findById(detail.getPoDetailId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy dòng PO: " + detail.getPoDetailId()));
                pod.addReceived(detail.getQuantity());
                poDetailRepository.save(pod);
            }

            // 2) Increase aggregate inventory (insert if first time, else accumulate).
            boolean exists = inventoryRepository
                    .findByProductIdAndWarehouseId(detail.getProductId(), grn.getWarehouseId())
                    .isPresent();
            if (!exists) {
                inventoryDomainService.initInventory(detail.getProductId(), grn.getWarehouseId());
            }
            // [GIAI ĐOẠN 6] chụp tồn tổng trước/sau để ghi thẻ kho khớp DDL stock_movements.
            int quantityBefore = inventoryRepository
                    .findByProductIdAndWarehouseId(detail.getProductId(), grn.getWarehouseId())
                    .map(Inventory::getQuantity).orElse(0);
            inventoryDomainService.addStock(detail.getProductId(), grn.getWarehouseId(), detail.getQuantity());
            int quantityAfter = quantityBefore + detail.getQuantity();

            // 3) Upsert physical batch at the bin (uq_product_batch_location).
            String batchId = upsertBatch(detail, grn.getWarehouseId());

            // 4) Append the audit-trail stock movement (IN / GOODS_RECEIPT).
            StockMovement movement = StockMovement.builder()
                    .id(idGenerator.generate())
                    .productId(detail.getProductId())
                    .warehouseId(grn.getWarehouseId())
                    .batchId(batchId)
                    .movementType(StockMovement.MovementType.IN)
                    .quantityChange(detail.getQuantity())
                    .quantityBefore(quantityBefore)
                    .quantityAfter(quantityAfter)
                    .referenceType("GOODS_RECEIPT")
                    .referenceId(grn.getId())
                    .note("Nhập kho từ phiếu " + grn.getGrnNumber())
                    .createdBy(grn.getReceivedBy())
                    .build();
            stockMovementRepository.save(movement);
        }

        // Auto-close the PO when every line has been fully received.
        if (grn.isLinkedToPurchaseOrder()) {
            List<PurchaseOrderDetail> poLines = poDetailRepository.findByPoId(grn.getPoId());
            boolean allReceived = !poLines.isEmpty()
                    && poLines.stream().allMatch(PurchaseOrderDetail::isFullyReceived);
            if (allReceived) {
                PurchaseOrder po = poRepository.findById(grn.getPoId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mua: " + grn.getPoId()));
                po.close();
                poRepository.save(po);
            }
        }

        return grnRepository.save(grn);
    }

    /**
     * Finds an existing ACTIVE batch matching (product, batchNumber, binLocation) and
     * accumulates onto it; otherwise inserts a brand-new ACTIVE batch.
     * Returns the batch id (null when the receipt line carries no batch number).
     */
    private String upsertBatch(GoodsReceiptDetail detail, String warehouseId) {
        if (!detail.hasBatch()) {
            return null;
        }
        Optional<InventoryBatch> existing = batchRepository
                .findByProductIdAndWarehouseId(detail.getProductId(), warehouseId)
                .stream()
                .filter(b -> detail.getBatchNumber().equals(b.getBatchNumber())
                        && detail.getBinLocationId().equals(b.getBinLocationId()))
                .findFirst();

        if (existing.isPresent()) {
            InventoryBatch batch = existing.get();
            batch.addQuantity(detail.getQuantity());
            batchRepository.save(batch);
            return batch.getId();
        }

        InventoryBatch newBatch = new InventoryBatch.Builder()
                .id(idGenerator.generate())
                .productId(detail.getProductId())
                .warehouseId(warehouseId)
                .binLocationId(detail.getBinLocationId())
                .batchNumber(detail.getBatchNumber())
                .quantity(detail.getQuantity())
                .expiryDate(detail.getExpiryDate())
                .manufacturedDate(null) // goods_receipt_details has no manufactured_date column
                .status(InventoryBatch.Status.ACTIVE)
                .build();
        batchRepository.save(newBatch);
        return newBatch.getId();
    }

    private String generateGrnNumber() {
        String number;
        do {
            number = "GRN-" + idGenerator.generate();
        } while (grnRepository.existsByGrnNumber(number));
        return number;
    }
}
