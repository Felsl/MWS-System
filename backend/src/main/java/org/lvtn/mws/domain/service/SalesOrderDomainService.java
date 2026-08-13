package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.*;
import org.lvtn.mws.domain.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Nghiệp vụ Đơn bán hàng. Thuần Java — không phụ thuộc Spring.
 * Bao gồm: tạo đơn DRAFT, phân bổ hàng ảo (reserveStock -> ALLOCATED), hủy đơn (release).
 */
public class SalesOrderDomainService {

    private final ISalesOrderRepository soRepository;
    private final IInventoryRepository inventoryRepository;
    private final IUserRepository userRepository;
    private final org.lvtn.mws.domain.repository.IInventoryBatchRepository batchRepository;
    private final org.lvtn.mws.domain.repository.IStockDemandRepository demandRepository;
    private final IIdGenerator idGenerator;
    private final ISalesOrderNumberGenerator soNumberGenerator;

    public SalesOrderDomainService(ISalesOrderRepository soRepository,
                                   IInventoryRepository inventoryRepository,
                                   org.lvtn.mws.domain.repository.IInventoryBatchRepository batchRepository,
                                   org.lvtn.mws.domain.repository.IStockDemandRepository demandRepository,
                                   IIdGenerator idGenerator,
                                   ISalesOrderNumberGenerator soNumberGenerator,
                                   IUserRepository userRepository
                                   ) {
        this.soRepository       = soRepository;
        this.inventoryRepository = inventoryRepository;
        this.batchRepository    = batchRepository;
        this.demandRepository   = demandRepository;
        this.idGenerator        = idGenerator;
        this.userRepository = userRepository;
        this.soNumberGenerator  = soNumberGenerator;
    }

    public SalesOrder create(String warehouseId,
                             String customerId,
                             BigDecimal discountAmount,
                             int priority,
                             LocalDate requiredDate,
                             String createdBy,
                             List<SalesOrderLineCommand> lines) {
        BigDecimal totalDiscountAmount=BigDecimal.ZERO,totalAmount= BigDecimal.ZERO;

        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Đơn bán hàng phải có ít nhất 1 dòng sản phẩm");
        }
        for (SalesOrderLineCommand line : lines) {
            totalAmount = BigDecimal.valueOf(line.quantityOrdered()).multiply(line.unitPrice());
            totalDiscountAmount = totalAmount.multiply(line.discountPercent().divide(BigDecimal.valueOf(100)));
        }

        String soId = idGenerator.generate();
        SalesOrder so = new SalesOrder.Builder()
                .id(soId)
                .soNumber(soNumberGenerator.next())
                .warehouseId(warehouseId)
                .customerId(customerId)
                .discountAmount(totalDiscountAmount)
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
                    .supplierId(line.supplierId())
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
        List<SalesOrder> findAll =soRepository.findAll();
        List<SalesOrder> newList = new ArrayList<SalesOrder>();

        for(SalesOrder sl : findAll){
            String createBy = userRepository.findById(sl.getCreatedBy()).map(User::getUsername).orElse("-");
            SalesOrder temp = new SalesOrder.Builder()
                    .id(sl.getId())
                    .soNumber(sl.getSoNumber())
                    .warehouseId(sl.getWarehouseId())
                    .customerId(sl.getCustomerId())
                    .discountAmount(sl.getDiscountAmount())
                    .priority(sl.getPriority())
                    .requiredDate(sl.getRequiredDate())
                    .status(SalesOrder.Status.DRAFT)
                    .createdBy(createBy)
                    .build();
            newList.add(temp);
        }
        return newList;
    }

    /** [A2] Danh sách đơn xuất đã lọc theo phạm vi kho của user. */
    public List<SalesOrder> findAllScoped() {
        return soRepository.findAllScoped();
    }

    /** [B4] Tìm kiếm + phân trang (đã lọc theo phạm vi kho). */
    public org.lvtn.mws.domain.common.PageResult<SalesOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery) {
//        org.lvtn.mws.domain.common.PageResult<SalesOrder> pageResult = soRepository.search(keyword, status, pageQuery);
//        java.util.List<SalesOrder> domainList = pageResult.content().stream()
//                .toList();
//
//        List<SalesOrder> newList = new ArrayList<SalesOrder>();
//
//        for(SalesOrder sl : domainList){
//            String createBy = userRepository.findById(sl.getCreatedBy()).map(User::getUsername).orElse("-");
//            SalesOrder temp = new SalesOrder.Builder()
//                    .id(sl.getId())
//                    .soNumber(sl.getSoNumber())
//                    .warehouseId(sl.getWarehouseId())
//                    .customerId(sl.getCustomerId())
//                    .discountAmount(sl.getDiscountAmount())
//                    .priority(sl.getPriority())
//                    .requiredDate(sl.getRequiredDate())
//                    .status(SalesOrder.Status.DRAFT)
//                    .createdBy(createBy)
//                    .build();
//            newList.add(temp);
//        }
//
//        return new org.lvtn.mws.domain.common.PageResult<>(newList.stream().toList(),
//        pageResult.page(), pageResult.size(), pageResult.totalElements());
        return soRepository.search(keyword, status, pageQuery);
    }

    /**
     * [Bán vượt tồn — Pha 2] Phân bổ MỘT PHẦN theo lô của đúng NCC (FEFO).
     * Giữ được bao nhiêu thì set quantityAllocated bấy nhiêu; phần còn thiếu ghi StockDemand(OPEN).
     * Đơn KHÔNG bị chặn: đủ hết -> ALLOCATED; có dòng thiếu -> PARTIALLY_ALLOCATED (chờ hàng về bù).
     */
    public SalesOrder allocate(String soId) {
        SalesOrder so = findById(soId);
        boolean anyShort = false;
        for (SalesOrderDetail detail : so.getDetails()) {
            int ordered = detail.getQuantityOrdered();
            String supplierId = detail.getSupplierId();

            List<org.lvtn.mws.domain.model.InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPickingBySupplier(
                            detail.getProductId(), so.getWarehouseId(), supplierId);
            int need = ordered, allocated = 0;
            for (org.lvtn.mws.domain.model.InventoryBatch batch : batches) {
                if (need <= 0) break;
                int take = Math.min(batch.availableQuantity(), need);
                if (take > 0) {
                    batch.reserve(take);
                    batchRepository.save(batch);
                    need -= take;
                    allocated += take;
                }
            }

            if (allocated > 0) {
                Inventory inv = inventoryRepository
                        .findByProductIdAndWarehouseId(detail.getProductId(), so.getWarehouseId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Chưa có bản ghi tồn kho cho sản phẩm " + detail.getProductId()
                                        + " tại kho " + so.getWarehouseId()));
                inv.reserve(allocated);
                inventoryRepository.save(inv);
            }
            detail.allocate(allocated);

            if (need > 0) {
                anyShort = true;
                demandRepository.save(org.lvtn.mws.domain.model.StockDemand.builder()
                        .id(idGenerator.generate())
                        .soId(so.getId())
                        .soDetailId(detail.getId())
                        .productId(detail.getProductId())
                        .warehouseId(so.getWarehouseId())
                        .supplierId(supplierId)
                        .quantityShort(need)
                        .status(org.lvtn.mws.domain.model.StockDemand.Status.OPEN)
                        .build());
            }
        }
        if (anyShort) so.allocatePartial();
        else so.allocate();
        return soRepository.save(so);
    }

    /**
     * [Bán vượt tồn] Bù backorder khi HÀNG VỀ cho (sản phẩm, kho): quét nhu cầu OPEN theo FIFO,
     * giữ chỗ phần vừa về vào lô của đúng NCC, tăng quantity_allocated của dòng bán, giảm nhu cầu;
     * nếu đơn đã đủ toàn bộ -> PARTIALLY_ALLOCATED chuyển ALLOCATED. Trả về danh sách soId được bù.
     */
    public List<String> fulfillDemands(String productId, String warehouseId) {
        java.util.LinkedHashSet<String> touched = new java.util.LinkedHashSet<>();
        for (org.lvtn.mws.domain.model.StockDemand d :
                demandRepository.findOpenByProductAndWarehouse(productId, warehouseId)) {
            if (d.getQuantityShort() <= 0) continue;

            List<org.lvtn.mws.domain.model.InventoryBatch> batches =
                    batchRepository.findActiveBatchesForPickingBySupplier(productId, warehouseId, d.getSupplierId());
            int need = d.getQuantityShort(), taken = 0;
            for (org.lvtn.mws.domain.model.InventoryBatch batch : batches) {
                if (need <= 0) break;
                int take = Math.min(batch.availableQuantity(), need);
                if (take > 0) {
                    batch.reserve(take);
                    batchRepository.save(batch);
                    need -= take;
                    taken += take;
                }
            }
            if (taken <= 0) continue;

            var invOpt = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
            if (invOpt.isPresent()) {
                Inventory inv = invOpt.get();
                inv.reserve(taken);
                inventoryRepository.save(inv);
            }

            var soOpt = soRepository.findById(d.getSoId());
            if (soOpt.isPresent()) {
                SalesOrder so = soOpt.get();
                for (SalesOrderDetail det : so.getDetails()) {
                    if (det.getId().equals(d.getSoDetailId())) {
                        det.allocate(det.getQuantityAllocated() + taken);
                    }
                }
                boolean allFull = so.getDetails().stream()
                        .allMatch(x -> x.getQuantityAllocated() >= x.getQuantityOrdered());
                if (allFull) so.markFullyAllocated();
                soRepository.save(so);
                touched.add(so.getId());
            }

            d.reduce(taken);
            demandRepository.save(d);
        }
        return new java.util.ArrayList<>(touched);
    }

    /** Hủy đơn: giải phóng phần đã giữ chỗ (cả tồn tổng LẪN theo lô) nếu đang ALLOCATED/PICKING. */
    public SalesOrder cancel(String soId) {
        SalesOrder so = findById(soId);
        if (so.wasReserved()) {
            for (SalesOrderDetail detail : so.getDetails()) {
                // Nhả tồn tổng như trước (theo số đặt).
                inventoryRepository
                        .findByProductIdAndWarehouseId(detail.getProductId(), so.getWarehouseId())
                        .ifPresent(inv -> {
                            inv.release(detail.getQuantityOrdered());
                            inventoryRepository.save(inv);
                        });

                // Nhả giữ chỗ theo lô của đúng NCC (theo số đã phân bổ). Đơn cũ (Pha 0)
                // có quantityAllocated=0 nên vòng lặp không làm gì -> an toàn ngược tương thích.
                int toRelease = detail.getQuantityAllocated();
                if (toRelease > 0) {
                    List<org.lvtn.mws.domain.model.InventoryBatch> batches =
                            batchRepository.findActiveBatchesForPickingBySupplier(
                                    detail.getProductId(), so.getWarehouseId(), detail.getSupplierId());
                    int rem = toRelease;
                    for (org.lvtn.mws.domain.model.InventoryBatch batch : batches) {
                        if (rem <= 0) break;
                        int give = Math.min(batch.getReservedQuantity(), rem);
                        if (give > 0) {
                            batch.release(give);
                            batchRepository.save(batch);
                            rem -= give;
                        }
                    }
                }
            }
        }
        so.cancel();
        // [Bán vượt tồn] Hủy các nhu cầu OPEN còn treo của đơn này.
        for (org.lvtn.mws.domain.model.StockDemand d : demandRepository.findOpenBySoId(soId)) {
            d.cancel();
            demandRepository.save(d);
        }
        return soRepository.save(so);
    }
}
