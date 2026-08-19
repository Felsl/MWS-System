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
 * Unit test TẦNG 2 (Mockito) cho orchestration đơn xuất [Bán theo NCC]:
 * allocate giữ chỗ THEO LÔ (FEFO theo NCC) + phân bổ một phần + tạo nhu cầu (backorder);
 * cancel giải phóng. Mock repository — không Spring, không DB.
 */
@ExtendWith(MockitoExtension.class)
class SalesOrderDomainServiceTest {

    @Mock ISalesOrderRepository soRepository;
    @Mock IInventoryRepository inventoryRepository;
    @Mock IInventoryBatchRepository batchRepository;
    @Mock IStockDemandRepository demandRepository;
    @Mock IIdGenerator idGenerator;
    @Mock ISalesOrderNumberGenerator soNumberGenerator;

    @InjectMocks SalesOrderDomainService service;

    private SalesOrderDetail line(int qty) {
        return new SalesOrderDetail.Builder()
                .id("D1").soId("SO1").productId("P1").quantityOrdered(qty).build();
    }

    private SalesOrder order(SalesOrder.Status status, int qty) {
        return new SalesOrder.Builder()
                .id("SO1").soNumber("SO-001").warehouseId("W1").customerId("C1")
                .status(status).createdBy("u1")
                .details(java.util.List.of(line(qty)))
                .build();
    }

    private Inventory inv(int quantity, int reserved) {
        return new Inventory.Builder()
                .productId("P1").warehouseId("W1")
                .quantity(quantity).reservedQuantity(reserved).version(0).build();
    }

    private InventoryBatch batch(String id, int qty) {
        return new InventoryBatch.Builder()
                .id(id).productId("P1").warehouseId("W1").binLocationId("B1")
                .batchNumber("LOT-" + id).quantity(qty)
                .status(InventoryBatch.Status.ACTIVE).build();
    }

    @Test
    void allocateReservesStockAndPersists() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5);
        Inventory inventory = inv(10, 0);
        InventoryBatch b = batch("BB", 10);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of(b));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        SalesOrder result = service.allocate("SO1");

        assertThat(b.getReservedQuantity()).isEqualTo(5);
        assertThat(inventory.getReservedQuantity()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(SalesOrder.Status.ALLOCATED);
        assertThat(result.getDetails().get(0).getQuantityAllocated()).isEqualTo(5);
        verify(inventoryRepository).save(inventory);
        verify(soRepository).save(so);
    }

    @Test
    void allocatePartialCreatesDemand() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5);
        Inventory inventory = inv(3, 0);
        InventoryBatch b = batch("BB", 3);            // chỉ đủ 3/5
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of(b));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(idGenerator.generate()).thenReturn("DMD1");
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        SalesOrder result = service.allocate("SO1");

        assertThat(b.getReservedQuantity()).isEqualTo(3);
        assertThat(inventory.getReservedQuantity()).isEqualTo(3);
        assertThat(result.getDetails().get(0).getQuantityAllocated()).isEqualTo(3);
        assertThat(result.getStatus()).isEqualTo(SalesOrder.Status.PARTIALLY_ALLOCATED);
        verify(demandRepository).save(any(StockDemand.class));   // ghi nhu cầu phần thiếu
    }

    @Test
    void allocateMissingInventoryRow_stillAllocatesFromBatches() {
        // Tồn tổng lệch/thiếu KHÔNG được làm rollback: LÔ là nguồn sự thật để nhặt.
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5);
        InventoryBatch b = batch("BB", 5);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(batchRepository.findActiveBatchesForPickingBySupplier(eq("P1"), eq("W1"), isNull()))
                .thenReturn(List.of(b));
        when(batchRepository.save(any(InventoryBatch.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.empty());   // KHÔNG có bản ghi tồn tổng
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        SalesOrder result = service.allocate("SO1");

        assertThat(result.getStatus()).isEqualTo(SalesOrder.Status.ALLOCATED);
        assertThat(result.getDetails().get(0).getQuantityAllocated()).isEqualTo(5);
        assertThat(b.getReservedQuantity()).isEqualTo(5);
        verify(inventoryRepository, never()).save(any());   // bỏ qua tồn tổng, không ném
    }

    @Test
    void cancelAllocatedReleasesReserved() {
        SalesOrder so = order(SalesOrder.Status.ALLOCATED, 5);
        Inventory inventory = inv(10, 5);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(demandRepository.findOpenBySoId("SO1")).thenReturn(List.of());
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        service.cancel("SO1");

        assertThat(inventory.getReservedQuantity()).isEqualTo(0);
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.CANCELLED);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void cancelDraftDoesNotTouchInventory() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5); // wasReserved == false
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(demandRepository.findOpenBySoId("SO1")).thenReturn(List.of());
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        service.cancel("SO1");

        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.CANCELLED);
        verify(inventoryRepository, never()).findByProductIdAndWarehouseId(any(), any());
        verify(inventoryRepository, never()).save(any());
    }
}
