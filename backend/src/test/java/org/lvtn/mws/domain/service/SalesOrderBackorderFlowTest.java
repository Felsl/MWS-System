package org.lvtn.mws.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.domain.model.StockDemand;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.ISalesOrderNumberGenerator;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.domain.repository.IStockDemandRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * [Bán vượt tồn — kiểm tra RIÊNG luồng backorder]
 * Phủ 3 hành vi người dùng báo "không hoạt động":
 *   (1) Tạo đơn + phân bổ khi ĐẶT > TỒN  -> giữ được một phần, ghi nhu cầu, đơn PARTIALLY_ALLOCATED.
 *   (2) Khi bù đủ (hàng về) -> nhu cầu FULFILLED, đơn chuyển ALLOCATED.
 *   (3) Khi bù thiếu -> nhu cầu giảm dần, đơn vẫn PARTIALLY_ALLOCATED (tự phân bổ lại nhiều lần).
 * Test tầng domain (Mockito) — không cần Spring/DB, chạy nhanh, độc lập.
 */
@ExtendWith(MockitoExtension.class)
class SalesOrderBackorderFlowTest {

    @Mock ISalesOrderRepository soRepository;
    @Mock IInventoryRepository inventoryRepository;
    @Mock IInventoryBatchRepository batchRepository;
    @Mock IStockDemandRepository demandRepository;
    @Mock IIdGenerator idGenerator;
    @Mock ISalesOrderNumberGenerator soNumberGenerator;

    @InjectMocks SalesOrderDomainService service;

    // ----- helpers -----
    private SalesOrder draftOrder(int ordered) {
        SalesOrderDetail d = new SalesOrderDetail.Builder()
                .id("D1").soId("SO1").productId("P1").quantityOrdered(ordered).build();
        return new SalesOrder.Builder()
                .id("SO1").soNumber("SO-001").warehouseId("W1").customerId("C1")
                .status(SalesOrder.Status.DRAFT).createdBy("u1")
                .details(List.of(d)).build();
    }

    private SalesOrder partialOrder(int ordered, int allocatedSoFar) {
        SalesOrderDetail d = new SalesOrderDetail.Builder()
                .id("D1").soId("SO1").productId("P1").quantityOrdered(ordered).build();
        d.allocate(allocatedSoFar);
        return new SalesOrder.Builder()
                .id("SO1").soNumber("SO-001").warehouseId("W1").customerId("C1")
                .status(SalesOrder.Status.PARTIALLY_ALLOCATED).createdBy("u1")
                .details(List.of(d)).build();
    }

    private Inventory inv(int quantity, int reserved) {
        return new Inventory.Builder().productId("P1").warehouseId("W1")
                .quantity(quantity).reservedQuantity(reserved).version(0).build();
    }

    private InventoryBatch batch(String id, int qty, String supplier) {
        return new InventoryBatch.Builder()
                .id(id).productId("P1").warehouseId("W1").binLocationId("B1")
                .batchNumber("LOT-" + id).quantity(qty).supplierId(supplier)
                .status(InventoryBatch.Status.ACTIVE).build();
    }

    private StockDemand openDemand(int shortQty, String supplier) {
        return StockDemand.builder()
                .id("DMD1").soId("SO1").soDetailId("D1").productId("P1").warehouseId("W1")
                .supplierId(supplier).quantityShort(shortQty)
                .status(StockDemand.Status.OPEN).build();
    }

    // ===================== (1) ĐẶT > TỒN =====================

    @Test
    void allocate_orderGreaterThanStock_partialAndCreatesDemand() {
        SalesOrder so = draftOrder(10);
        Inventory inventory = inv(4, 0);
        InventoryBatch b = batch("BB", 4, null);   // chỉ có 4/10
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of(b));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(idGenerator.generate()).thenReturn("DMD1");
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        SalesOrder result = service.allocate("SO1");

        assertThat(result.getStatus()).isEqualTo(SalesOrder.Status.PARTIALLY_ALLOCATED);
        assertThat(result.getDetails().get(0).getQuantityAllocated()).isEqualTo(4);
        assertThat(b.getReservedQuantity()).isEqualTo(4);
        assertThat(inventory.getReservedQuantity()).isEqualTo(4);
        verify(demandRepository).save(any(StockDemand.class));   // nhu cầu 6 được ghi
    }

    @Test
    void allocate_noStockAtAll_fullyBackordered() {
        SalesOrder so = draftOrder(5);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of());                          // không có lô nào
        when(idGenerator.generate()).thenReturn("DMD1");
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        SalesOrder result = service.allocate("SO1");

        assertThat(result.getStatus()).isEqualTo(SalesOrder.Status.PARTIALLY_ALLOCATED);
        assertThat(result.getDetails().get(0).getQuantityAllocated()).isZero();
        verify(demandRepository).save(any(StockDemand.class));
        // Không có gì để giữ chỗ -> KHÔNG đụng tồn tổng (tránh orElseThrow khi thiếu bản ghi inventory)
        verify(inventoryRepository, never()).findByProductIdAndWarehouseId(any(), any());
    }

    // ===================== (2)+(3) HÀNG VỀ -> TỰ PHÂN BỔ LẠI =====================

    @Test
    void fulfill_fullArrival_completesDemandAndMovesToAllocated() {
        StockDemand demand = openDemand(6, null);
        SalesOrder so = partialOrder(10, 4);        // đã giữ 4, còn thiếu 6
        Inventory inventory = inv(10, 4);           // 6 mới về đang khả dụng
        InventoryBatch fresh = batch("NEW", 6, null);
        when(demandRepository.findOpenByProductAndWarehouse("P1", "W1")).thenReturn(List.of(demand));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of(fresh));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(demandRepository.save(any(StockDemand.class))).thenAnswer(i -> i.getArgument(0));

        List<String> touched = service.fulfillDemands("P1", "W1");

        assertThat(touched).containsExactly("SO1");
        assertThat(fresh.getReservedQuantity()).isEqualTo(6);
        assertThat(inventory.getReservedQuantity()).isEqualTo(10);
        assertThat(so.getDetails().get(0).getQuantityAllocated()).isEqualTo(10);
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.ALLOCATED);   // đủ hàng -> ALLOCATED
        assertThat(demand.getQuantityShort()).isZero();
        assertThat(demand.getStatus()).isEqualTo(StockDemand.Status.FULFILLED);
    }

    @Test
    void fulfill_partialArrival_reducesDemandStaysPartial() {
        StockDemand demand = openDemand(6, null);
        SalesOrder so = partialOrder(10, 4);
        Inventory inventory = inv(10, 4);
        InventoryBatch fresh = batch("NEW", 4, null);   // chỉ về 4/6
        when(demandRepository.findOpenByProductAndWarehouse("P1", "W1")).thenReturn(List.of(demand));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of(fresh));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));
        when(demandRepository.save(any(StockDemand.class))).thenAnswer(i -> i.getArgument(0));

        service.fulfillDemands("P1", "W1");

        assertThat(fresh.getReservedQuantity()).isEqualTo(4);
        assertThat(so.getDetails().get(0).getQuantityAllocated()).isEqualTo(8);
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.PARTIALLY_ALLOCATED); // vẫn thiếu 2
        assertThat(demand.getQuantityShort()).isEqualTo(2);
        assertThat(demand.getStatus()).isEqualTo(StockDemand.Status.OPEN);
    }

    @Test
    void fulfill_supplierMismatch_noRefill() {
        StockDemand demand = openDemand(6, "SUP_X");     // đơn cần NCC X
        when(demandRepository.findOpenByProductAndWarehouse("P1", "W1")).thenReturn(List.of(demand));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), eq("SUP_X")))
                .thenReturn(List.of());                       // hàng về không phải NCC X

        List<String> touched = service.fulfillDemands("P1", "W1");

        assertThat(touched).isEmpty();
        assertThat(demand.getQuantityShort()).isEqualTo(6);   // không đổi
        verify(soRepository, never()).findById(any());
        verify(demandRepository, never()).save(any());
    }
}
