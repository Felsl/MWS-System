package org.lvtn.mws.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lvtn.mws.domain.model.InsufficientStockException;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrderDetail;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.ISalesOrderNumberGenerator;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test TẦNG 2 (Mockito) cho orchestration đơn xuất: allocate giữ chỗ tồn kho,
 * cancel giải phóng. Mock repository — không Spring, không DB.
 */
@ExtendWith(MockitoExtension.class)
class SalesOrderDomainServiceTest {

    @Mock ISalesOrderRepository soRepository;
    @Mock IInventoryRepository inventoryRepository;
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
                .details(java.util.List.of(line(qty))) // dựng qua builder, bỏ qua guard addDetail chỉ-DRAFT
                .build();
    }

    private Inventory inv(int quantity, int reserved) {
        return new Inventory.Builder()
                .productId("P1").warehouseId("W1")
                .quantity(quantity).reservedQuantity(reserved).version(0).build();
    }

    @Test
    void allocateReservesStockAndPersists() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5);
        Inventory inventory = inv(10, 0);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        SalesOrder result = service.allocate("SO1");

        assertThat(inventory.getReservedQuantity()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(SalesOrder.Status.ALLOCATED);
        verify(inventoryRepository).save(inventory);
        verify(soRepository).save(so);
    }

    @Test
    void allocateInsufficientStockPropagatesAndDoesNotAllocate() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5);
        Inventory inventory = inv(3, 0); // khả dụng 3 < 5
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> service.allocate("SO1"))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.DRAFT); // chưa chuyển trạng thái
        verify(soRepository, never()).save(any());                    // không lưu đơn -> tx rollback
    }

    @Test
    void allocateMissingInventoryThrows() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, 5);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.allocate("SO1"))
                .isInstanceOf(IllegalStateException.class);
        verify(soRepository, never()).save(any());
    }

    @Test
    void cancelAllocatedReleasesReserved() {
        SalesOrder so = order(SalesOrder.Status.ALLOCATED, 5);
        Inventory inventory = inv(10, 5);
        when(soRepository.findById("SO1")).thenReturn(Optional.of(so));
        when(inventoryRepository.findByProductIdAndWarehouseId("P1", "W1"))
                .thenReturn(Optional.of(inventory));
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
        when(soRepository.save(any(SalesOrder.class))).thenAnswer(i -> i.getArgument(0));

        service.cancel("SO1");

        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.CANCELLED);
        verify(inventoryRepository, never()).findByProductIdAndWarehouseId(any(), any());
        verify(inventoryRepository, never()).save(any());
    }
}
