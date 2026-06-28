package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.model.InsufficientStockException;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.model.TransferReceiptLine;
import org.lvtn.mws.domain.repository.ICarrierRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.INotificationPort;
import org.lvtn.mws.domain.repository.IShipmentRepository;
import org.lvtn.mws.domain.repository.IShippingFeeCalculator;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.domain.repository.ITransferOrderRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain service điều phối toàn bộ nghiệp vụ Internal Transfer (Giai đoạn 5).
 * THUẦN JAVA: chỉ phụ thuộc domain.model / domain.repository + JDK.
 * Ranh giới @Transactional được đặt ở tầng UseCase (giống module Product mẫu).
 */
public class TransferOrderDomainService {

    private static final String REF_TYPE = "TRANSFER_ORDER";
    /** Tác nhân ghi thẻ kho cho biến động điều chuyển (chưa thread user context vào hàm complete). */
    private static final String MOVEMENT_ACTOR = "SYSTEM";

    private final ITransferOrderRepository transferRepository;
    private final IShipmentRepository shipmentRepository;
    private final ICarrierRepository carrierRepository;
    private final IInventoryRepository inventoryRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IWarehouseRepository warehouseRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final INotificationPort notificationPort;
    private final IShippingFeeCalculator shippingFeeCalculator;
    private final IIdGenerator idGenerator;

    public TransferOrderDomainService(ITransferOrderRepository transferRepository,
                                      IShipmentRepository shipmentRepository,
                                      ICarrierRepository carrierRepository,
                                      IInventoryRepository inventoryRepository,
                                      IInventoryBatchRepository batchRepository,
                                      IWarehouseRepository warehouseRepository,
                                      IStockMovementRepository stockMovementRepository,
                                      INotificationPort notificationPort,
                                      IShippingFeeCalculator shippingFeeCalculator,
                                      IIdGenerator idGenerator) {
        this.transferRepository      = transferRepository;
        this.shipmentRepository      = shipmentRepository;
        this.carrierRepository       = carrierRepository;
        this.inventoryRepository     = inventoryRepository;
        this.batchRepository         = batchRepository;
        this.warehouseRepository     = warehouseRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.notificationPort        = notificationPort;
        this.shippingFeeCalculator   = shippingFeeCalculator;
        this.idGenerator             = idGenerator;
    }

    // ── Đọc ──────────────────────────────────────────────────────────────────

    public TransferOrder findById(String id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chuyển: " + id));
    }

    public List<TransferOrder> findAll() {
        return transferRepository.findAll();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // I. KHỞI TẠO PHIẾU (DRAFT)
    // ──────────────────────────────────────────────────────────────────────────

    /** Tạo phiếu DRAFT + bẫy lỗi trùng kho (chặn trước khi DB ném chk_diff_warehouse). */
    public TransferOrder createTransferOrder(String fromWarehouseId,
                                             String toWarehouseId,
                                             String createdBy,
                                             List<NewTransferLine> lines) {
        if (fromWarehouseId != null && fromWarehouseId.equals(toWarehouseId)) {
            throw new IllegalArgumentException("Kho nguồn và kho đích không được trùng nhau");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Phiếu điều chuyển phải có ít nhất 1 dòng hàng");
        }
        validateWarehouseExists(fromWarehouseId, "Kho nguồn");
        validateWarehouseExists(toWarehouseId, "Kho đích");

        String transferId = idGenerator.generate();
        String transferNumber = generateTransferNumber();

        List<TransferOrderDetail> details = new ArrayList<>();
        for (NewTransferLine line : lines) {
            details.add(new TransferOrderDetail.Builder()
                    .id(idGenerator.generate())
                    .transferOrderId(transferId)
                    .productId(line.getProductId())
                    .quantity(line.getQuantity())
                    .quantityReceived(0)
                    .build());
        }

        TransferOrder order = new TransferOrder.Builder()
                .id(transferId)
                .fromWarehouseId(fromWarehouseId)
                .toWarehouseId(toWarehouseId)
                .transferNumber(transferNumber)
                .status(TransferOrder.Status.DRAFT)
                .createdBy(createdBy)
                .details(details)
                .build();

        return transferRepository.save(order);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // I.2 GỬI DUYỆT + GIỮ CHỖ ẢO KHO NGUỒN
    // ──────────────────────────────────────────────────────────────────────────

    /** DRAFT -> PENDING_APPROVAL. Tăng reserved_quantity ở kho nguồn để khoá hàng. */
    public TransferOrder requestTransferApproval(String transferId) {
        TransferOrder order = findById(transferId);

        Map<String, Integer> qtyByProduct = aggregateBy(order.getDetails(),
                TransferOrderDetail::getProductId, TransferOrderDetail::getQuantity);

        for (Map.Entry<String, Integer> e : qtyByProduct.entrySet()) {
            Inventory inv = inventoryRepository
                    .findByProductIdAndWarehouseId(e.getKey(), order.getFromWarehouseId())
                    .orElseThrow(() -> new InsufficientStockException(
                            "Kho nguồn chưa có tồn cho sản phẩm: " + e.getKey()));
            inv.reserve(e.getValue()); // ném InsufficientStockException nếu khả dụng < cần
            inventoryRepository.save(inv);
        }

        order.requestApproval();
        return transferRepository.save(order);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HUỶ / TỪ CHỐI — đối xứng với GỬI DUYỆT: trả lại reserved_quantity kho nguồn
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Huỷ phiếu điều chuyển. Nếu phiếu đã giữ chỗ ảo (PENDING_APPROVAL/APPROVED) thì
     * nhả lại reserved_quantity ở kho nguồn để hàng quay về khả dụng.
     * Domain model TransferOrder.cancel() đã chặn huỷ khi IN_TRANSIT/COMPLETED.
     */
    public TransferOrder cancelTransfer(String transferId) {
        TransferOrder order = findById(transferId);
        releaseSourceReservationIfHeld(order);
        order.cancel();
        return transferRepository.save(order);
    }

    /**
     * Từ chối duyệt phiếu (chỉ từ PENDING_APPROVAL). Vì lúc gửi duyệt đã giữ chỗ ảo,
     * từ chối phải nhả lại reserved_quantity ở kho nguồn.
     */
    public TransferOrder rejectTransfer(String transferId, String rejectedBy) {
        TransferOrder order = findById(transferId);
        releaseSourceReservationIfHeld(order);
        order.reject(rejectedBy);
        return transferRepository.save(order);
    }

    /** Nhả reserved_quantity kho nguồn nếu phiếu đang ở trạng thái đã giữ chỗ ảo. */
    private void releaseSourceReservationIfHeld(TransferOrder order) {
        boolean wasReserved = order.getStatus() == TransferOrder.Status.PENDING_APPROVAL
                || order.getStatus() == TransferOrder.Status.APPROVED;
        if (!wasReserved) return;

        Map<String, Integer> qtyByProduct = aggregateBy(order.getDetails(),
                TransferOrderDetail::getProductId, TransferOrderDetail::getQuantity);

        for (Map.Entry<String, Integer> e : qtyByProduct.entrySet()) {
            inventoryRepository
                    .findByProductIdAndWarehouseId(e.getKey(), order.getFromWarehouseId())
                    .ifPresent(inv -> {
                        inv.release(e.getValue());
                        inventoryRepository.save(inv);
                    });
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // II.1 DUYỆT + FEFO (gán lô + ô kệ nguồn, chưa trừ kho vật lý)
    // ──────────────────────────────────────────────────────────────────────────

    /** PENDING_APPROVAL -> APPROVED. Chạy FEFO, gán batchId + fromBinLocationId (tách dòng theo lô). */
    public TransferOrder approveTransferOrder(String transferId, String approvedBy) {
        TransferOrder order = findById(transferId);

        // gộp nhu cầu theo product (phòng trường hợp trùng product nhiều dòng)
        Map<String, Integer> demandByProduct = aggregateBy(order.getDetails(),
                TransferOrderDetail::getProductId, TransferOrderDetail::getQuantity);

        List<TransferOrderDetail> allocated = new ArrayList<>();

        for (Map.Entry<String, Integer> e : demandByProduct.entrySet()) {
            String productId = e.getKey();
            int remaining = e.getValue();

            // FEFO: ACTIVE batches sắp xếp expiry ASC, created ASC
            List<InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPicking(productId, order.getFromWarehouseId());

            for (InventoryBatch batch : batches) {
                if (remaining <= 0) break;
                int avail = batch.getQuantity();
                if (avail <= 0) continue;
                int take = Math.min(avail, remaining);

                allocated.add(new TransferOrderDetail.Builder()
                        .id(idGenerator.generate())
                        .transferOrderId(order.getId())
                        .productId(productId)
                        .batchId(batch.getId())
                        .fromBinLocationId(batch.getBinLocationId())
                        .quantity(take)
                        .quantityReceived(0)
                        .build());

                remaining -= take;
            }

            if (remaining > 0) {
                throw new InsufficientStockException(
                        "Không đủ tồn theo lô (FEFO) cho sản phẩm " + productId
                                + " tại kho nguồn: còn thiếu " + remaining);
            }
        }

        order.approve(approvedBy, allocated);
        return transferRepository.save(order);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // II.2 + II.3 ĐÓNG GÓI, TẠO GIAO VẬN, TRỪ KHO NGUỒN -> IN_TRANSIT
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * APPROVED -> IN_TRANSIT.
     * Tạo shipment (transfer_order_id set, sales_order_id NULL -> đúng chk_shipment_source),
     * trừ kho vật lý kho nguồn (inventory + inventory_batches), đưa hàng lên trạng thái đi đường.
     */
    public Shipment dispatchTransferShipment(String transferId, String carrierId) {
        TransferOrder order = findById(transferId);

        Carrier carrier = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển: " + carrierId));
        if (!carrier.isActive()) {
            throw new IllegalStateException("Đơn vị vận chuyển đang ngừng hoạt động: " + carrier.getCode());
        }

        List<TransferOrderDetail> details = order.getDetails();

        // Tạo kiện hàng: PACKED
        Shipment shipment = new Shipment.Builder()
                .id(idGenerator.generate())
                .shipmentNumber(generateShipmentNumber())
                .transferOrderId(order.getId())
                .salesOrderId(null)               // kích hoạt đúng bẫy chk_shipment_source
                .carrierId(carrier.getId())
                .status(Shipment.Status.PACKED)
                .build();

        // Ước tính phí ship từ cấu hình JSON (không có cột lưu -> dùng cho log/notify)
        int totalQty = details.stream().mapToInt(TransferOrderDetail::getQuantity).sum();
        BigDecimal estimatedFee = shippingFeeCalculator.estimate(
                carrier.getShippingFeeRule(), totalQty,
                order.getFromWarehouseId(), order.getToWarehouseId());

        // Xe lăn bánh: PACKED -> SHIPPING
        shipment.ship();

        // Trừ kho vật lý tại kho nguồn (quantity & reserved giảm song song)
        Map<String, Integer> qtyByProduct = aggregateBy(details,
                TransferOrderDetail::getProductId, TransferOrderDetail::getQuantity);
        for (Map.Entry<String, Integer> e : qtyByProduct.entrySet()) {
            Inventory inv = inventoryRepository
                    .findByProductIdAndWarehouseId(e.getKey(), order.getFromWarehouseId())
                    .orElseThrow(() -> new InsufficientStockException(
                            "Không tìm thấy tồn kho nguồn cho sản phẩm: " + e.getKey()));
            inv.commitDeduction(e.getValue());
            inventoryRepository.save(inv);
        }

        // Trừ chính xác theo lô tại ô kệ nguồn
        for (TransferOrderDetail d : details) {
            InventoryBatch batch = batchRepository.findById(d.getBatchId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Lô đã phân bổ không còn tồn tại: " + d.getBatchId()));
            batch.deduct(d.getQuantity());
            batchRepository.save(batch);
        }

        order.markInTransit();

        Shipment savedShipment = shipmentRepository.save(shipment);
        transferRepository.save(order);

        // LƯU Ý: KHÔNG cộng kho đích ở bước này (hàng đang đi đường - In-Transit).
        // estimatedFee có thể đính kèm log/notification tuỳ nhu cầu:
        // notificationPort... (bỏ qua vì không bắt buộc)
        if (estimatedFee != null) {
            // chừa chỗ cho ghi log phí dự kiến
        }

        return savedShipment;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // III + IV CẬP BẾN, ĐỐI SOÁT HAO HỤT, PUTAWAY, ĐỒNG BỘ KHO ĐÍCH, THẺ KHO
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * IN_TRANSIT -> COMPLETED. Hàm @Transactional chặng cuối (đặt @Transactional ở UseCase):
     * 1) Đối soát hao hụt + cảnh báo; 2) Điền ô kệ đích; 3) Cộng kho tổng + kho lô đích;
     * 4) Sinh 2 thẻ kho song phương; 5) Đóng shipment DELIVERED.
     */
    public TransferOrder completeTransferReceipt(String transferId,
                                                 List<TransferReceiptLine> receiptLines) {
        TransferOrder order = findById(transferId);
        if (receiptLines == null || receiptLines.isEmpty()) {
            throw new IllegalArgumentException("Danh sách nhận hàng không được trống");
        }

        // 1 + 2: ghi nhận số nhận thực tế + ô kệ đích, đối soát hao hụt
        for (TransferReceiptLine rl : receiptLines) {
            TransferOrderDetail detail =
                    order.receiveLine(rl.getDetailId(), rl.getQuantityReceived(), rl.getBinLocationId());
            int lost = detail.lostQuantity();
            if (lost > 0) {
                notificationPort.sendTransferDiscrepancyAlert(
                        order.getId(), order.getTransferNumber(), detail.getProductId(),
                        detail.getQuantity(), detail.getQuantityReceived(), lost);
            }
        }

        List<TransferOrderDetail> details = order.getDetails();

        // 3a: cộng tồn kho tổng tại kho đích (theo product)
        Map<String, Integer> receivedByProduct = aggregateBy(details,
                TransferOrderDetail::getProductId, TransferOrderDetail::getQuantityReceived);
        for (Map.Entry<String, Integer> e : receivedByProduct.entrySet()) {
            int received = e.getValue();
            if (received <= 0) continue;
            inventoryRepository.findByProductIdAndWarehouseId(e.getKey(), order.getToWarehouseId())
                    .ifPresentOrElse(inv -> {
                        inv.addStock(received);
                        inventoryRepository.save(inv);
                    }, () -> {
                        Inventory created = new Inventory.Builder()
                                .productId(e.getKey())
                                .warehouseId(order.getToWarehouseId())
                                .quantity(received)
                                .reservedQuantity(0)
                                .build();
                        inventoryRepository.save(created);
                    });
        }

        // 3b: cộng/khởi tạo tồn theo lô tại kho đích (giữ nguyên batchNumber + expiry gốc)
        for (TransferOrderDetail d : details) {
            if (d.getQuantityReceived() <= 0) continue;
            InventoryBatch src = batchRepository.findById(d.getBatchId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy lô gốc để truy xuất batchNumber: " + d.getBatchId()));

            InventoryBatch destExisting = batchRepository
                    .findByProductIdAndWarehouseId(d.getProductId(), order.getToWarehouseId())
                    .stream()
                    .filter(b -> d.getBinLocationId().equals(b.getBinLocationId())
                            && src.getBatchNumber().equals(b.getBatchNumber()))
                    .findFirst()
                    .orElse(null);

            if (destExisting != null) {
                destExisting.addQuantity(d.getQuantityReceived());
                batchRepository.save(destExisting);
            } else {
                InventoryBatch newBatch = new InventoryBatch.Builder()
                        .id(idGenerator.generate())
                        .productId(d.getProductId())
                        .warehouseId(order.getToWarehouseId())
                        .binLocationId(d.getBinLocationId())
                        .batchNumber(src.getBatchNumber())
                        .quantity(d.getQuantityReceived())
                        .expiryDate(src.getExpiryDate())
                        .manufacturedDate(src.getManufacturedDate())
                        .status(InventoryBatch.Status.ACTIVE)
                        .build();
                batchRepository.save(newBatch);
            }
        }

        // 4: thẻ kho song phương (audit trail)
        List<StockMovement> movements = new ArrayList<>();
        for (TransferOrderDetail d : details) {
            movements.add(new StockMovement.Builder()
                    .id(idGenerator.generate())
                    .warehouseId(order.getFromWarehouseId())
                    .productId(d.getProductId())
                    .batchId(d.getBatchId())
                    .movementType(StockMovement.MovementType.TRANSFER_OUT)
                    .quantityChange(-d.getQuantity())
                    .referenceType(REF_TYPE)
                    .referenceId(order.getId())
                    .createdBy(MOVEMENT_ACTOR)
                    .build());
            movements.add(new StockMovement.Builder()
                    .id(idGenerator.generate())
                    .warehouseId(order.getToWarehouseId())
                    .productId(d.getProductId())
                    .batchId(d.getBatchId())
                    .movementType(StockMovement.MovementType.TRANSFER_IN)
                    .quantityChange(d.getQuantityReceived())
                    .referenceType(REF_TYPE)
                    .referenceId(order.getId())
                    .createdBy(MOVEMENT_ACTOR)
                    .build());
        }
        stockMovementRepository.appendAll(movements);

        // 5: hoàn tất phiếu + đóng shipment
        order.complete();
        TransferOrder saved = transferRepository.save(order);

        shipmentRepository.findByTransferOrderId(order.getId()).ifPresent(s -> {
            s.deliver();
            shipmentRepository.save(s);
        });

        return saved;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void validateWarehouseExists(String warehouseId, String label) {
        if (warehouseId == null || !warehouseRepository.existsById(warehouseId)) {
            throw new IllegalArgumentException(label + " không tồn tại: " + warehouseId);
        }
    }

    private String generateTransferNumber() {
        String number;
        do {
            number = "TO-" + idGenerator.generate().toUpperCase();
        } while (transferRepository.existsByTransferNumber(number));
        return number;
    }

    private String generateShipmentNumber() {
        return "SHIP-" + idGenerator.generate().toUpperCase();
    }

    private <T> Map<String, Integer> aggregateBy(List<T> items,
                                                 java.util.function.Function<T, String> keyFn,
                                                 java.util.function.ToIntFunction<T> valFn) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (T item : items) {
            map.merge(keyFn.apply(item), valFn.applyAsInt(item), Integer::sum);
        }
        return map;
    }
}
