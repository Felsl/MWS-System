package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.domain.model.SalesOrderLineCommand;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.ISalesOrderNumberGenerator;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Nghiệp vụ Đơn bán hàng. Thuần Java — không phụ thuộc Spring.
 * Bao gồm: tạo đơn DRAFT, phân bổ hàng ảo (reserveStock -> ALLOCATED), hủy đơn (release).
 */
public class SalesOrderDomainService {

    private final ISalesOrderRepository soRepository;
    private final IInventoryRepository inventoryRepository;
    private final IIdGenerator idGenerator;
    private final ISalesOrderNumberGenerator soNumberGenerator;

    public SalesOrderDomainService(ISalesOrderRepository soRepository,
                                   IInventoryRepository inventoryRepository,
                                   IIdGenerator idGenerator,
                                   ISalesOrderNumberGenerator soNumberGenerator) {
        this.soRepository       = soRepository;
        this.inventoryRepository = inventoryRepository;
        this.idGenerator        = idGenerator;
        this.soNumberGenerator  = soNumberGenerator;
    }

    public SalesOrder create(String warehouseId,
                             String customerId,
                             BigDecimal discountAmount,
                             int priority,
                             LocalDate requiredDate,
                             String createdBy,
                             List<SalesOrderLineCommand> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Đơn bán hàng phải có ít nhất 1 dòng sản phẩm");
        }
        String soId = idGenerator.generate();
        SalesOrder so = new SalesOrder.Builder()
                .id(soId)
                .soNumber(soNumberGenerator.next())
                .warehouseId(warehouseId)
                .customerId(customerId)
                .discountAmount(discountAmount)
                .priority(priority)
                .requiredDate(requiredDate)
                .status(SalesOrder.Status.DRAFT)
                .createdBy(createdBy)
                .build();

        for (SalesOrderLineCommand line : lines) {
            SalesOrderDetail detail = new SalesOrderDetail.Builder()
                    .id(idGenerator.generate())
                    .soId(soId)
                    .productId(line.productId())
                    .quantityOrdered(line.quantityOrdered())
                    .unitPrice(line.unitPrice())
                    .discountPercent(line.discountPercent())
                    .build();
            so.addDetail(detail);
        }
        return soRepository.save(so);
    }

    public SalesOrder findById(String id) {
        return soRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn bán hàng: " + id));
    }

    public List<SalesOrder> findAll() {
        return soRepository.findAll();
    }

    /**
     * Phân bổ hàng ảo: duyệt từng dòng, gọi reserve trên tồn kho tổng.
     * Nếu bất kỳ dòng nào không đủ -> ném InsufficientStockException để UseCase rollback toàn bộ.
     */
    public SalesOrder allocate(String soId) {
        SalesOrder so = findById(soId);
        for (SalesOrderDetail detail : so.getDetails()) {
            Inventory inv = inventoryRepository
                    .findByProductIdAndWarehouseId(detail.getProductId(), so.getWarehouseId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Chưa có bản ghi tồn kho cho sản phẩm " + detail.getProductId()
                                    + " tại kho " + so.getWarehouseId()));
            inv.reserve(detail.getQuantityOrdered()); // có thể ném InsufficientStockException
            inventoryRepository.save(inv);
        }
        so.allocate();
        return soRepository.save(so);
    }

    /** Hủy đơn: giải phóng phần đã giữ chỗ (nếu đang ALLOCATED/PICKING). */
    public SalesOrder cancel(String soId) {
        SalesOrder so = findById(soId);
        if (so.wasReserved()) {
            for (SalesOrderDetail detail : so.getDetails()) {
                inventoryRepository
                        .findByProductIdAndWarehouseId(detail.getProductId(), so.getWarehouseId())
                        .ifPresent(inv -> {
                            inv.release(detail.getQuantityOrdered());
                            inventoryRepository.save(inv);
                        });
            }
        }
        so.cancel();
        return soRepository.save(so);
    }
}
