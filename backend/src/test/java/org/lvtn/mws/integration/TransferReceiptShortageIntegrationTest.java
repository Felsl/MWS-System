package org.lvtn.mws.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.model.TransferReceiptLine;
import org.lvtn.mws.domain.repository.ICarrierRepository;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.lvtn.mws.domain.service.TransferPickingDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TẦNG 3 — ĐỐI SOÁT HAO HỤT khi nhận kho điều chuyển.
 *
 * <p>Xuất kho 15, nhưng kho đích chỉ nhận thực tế 13 (thiếu 2). Kiểm chứng:
 * <ul>
 *   <li>Kho đích chỉ được cộng ĐÚNG số nhận thực tế (13), không phải số đã xuất (15).</li>
 *   <li>Dòng bị thiếu ghi nhận {@code lostQuantity() > 0} (hệ thống bắn cảnh báo hao hụt qua log).</li>
 *   <li>Phiếu vẫn chốt về COMPLETED (đối soát xong).</li>
 * </ul>
 *
 * <p>{@code @Transactional}: mỗi test tự rollback. Yêu cầu Docker.
 */
@Transactional
class TransferReceiptShortageIntegrationTest extends AbstractIntegrationTest {

    private static final String SRC = "WH-SRC-SH";
    private static final String DST = "WH-DST-SH";
    private static final String PROD = "P-SH-1";
    private static final String CARRIER = "CARRIER-SH";

    @Autowired TransferOrderDomainService transferService;
    @Autowired TransferPickingDomainService pickingService;
    @Autowired IInventoryRepository inventoryRepository;
    @Autowired IInventoryBatchRepository batchRepository;
    @Autowired IPickingListRepository pickingRepository;
    @Autowired ICarrierRepository carrierRepository;
    @PersistenceContext EntityManager em;

    @Test
    @DisplayName("Nhận thiếu (13/15): kho đích chỉ cộng 13, có ghi nhận hao hụt, phiếu vẫn COMPLETED")
    void receiveLessThanDispatched_reconcilesShortage() {
        // Seed 2 kho + tồn nguồn 100 + 2 lô (A cận hạn 10, B hạn xa 100).
        em.createNativeQuery("INSERT INTO warehouses (id, code, name, address) VALUES (?,?,?,?)")
                .setParameter(1, SRC).setParameter(2, "SRC-SH")
                .setParameter(3, "Kho nguồn").setParameter(4, "Địa chỉ").executeUpdate();
        em.createNativeQuery("INSERT INTO warehouses (id, code, name, address) VALUES (?,?,?,?)")
                .setParameter(1, DST).setParameter(2, "DST-SH")
                .setParameter(3, "Kho đích").setParameter(4, "Địa chỉ").executeUpdate();
        em.flush();
        carrierRepository.save(new Carrier.Builder()
                .id(CARRIER).code("GHN-SH").name("GHN")
                .shippingFeeRule("{\"baseFee\":20000,\"perUnitFee\":1500}")
                .status(Carrier.Status.ACTIVE).build());
        inventoryRepository.save(new Inventory.Builder()
                .productId(PROD).warehouseId(SRC).quantity(100).reservedQuantity(0).version(0).build());
        batchRepository.save(new InventoryBatch.Builder()
                .id("SH-A").productId(PROD).warehouseId(SRC).binLocationId("BIN-A")
                .batchNumber("LOT-SH-A").quantity(10).expiryDate(LocalDate.now().plusDays(10))
                .status(InventoryBatch.Status.ACTIVE).createdAt(LocalDateTime.now()).version(0).build());
        batchRepository.save(new InventoryBatch.Builder()
                .id("SH-B").productId(PROD).warehouseId(SRC).binLocationId("BIN-B")
                .batchNumber("LOT-SH-B").quantity(100).expiryDate(LocalDate.now().plusDays(60))
                .status(InventoryBatch.Status.ACTIVE).createdAt(LocalDateTime.now()).version(0).build());

        // create -> request -> approve -> picking (FEFO tách 10 + 5) -> quét -> dispatch
        TransferOrder created = transferService.createTransferOrder(
                SRC, DST, "u1", List.of(new NewTransferLine(PROD, 15)));
        transferService.requestTransferApproval(created.getId());
        transferService.approveTransferOrder(created.getId(), "boss");

        PickingList pl = pickingService.generateForTransfer(created.getId());
        for (PickingListDetail d : pl.getDetails()) {
            pickingService.confirmScanForTransfer(d.getId(), d.getBatchId(), "picker");
        }
        transferService.dispatchTransferShipment(created.getId(), CARRIER);

        // Nhận THIẾU: mỗi dòng nhận ít hơn 1 so với số xuất -> tổng nhận 13/15.
        TransferOrder inTransit = transferService.findById(created.getId());
        List<TransferOrderDetail> alloc = inTransit.getDetails();
        int dispatched = alloc.stream().mapToInt(TransferOrderDetail::getQuantity).sum();
        assertThat(dispatched).isEqualTo(15);

        // Nhận thiếu 2 đơn vị ở dòng đầu tiên.
        List<TransferReceiptLine> receipt = new java.util.ArrayList<>();
        boolean firstShorted = false;
        for (TransferOrderDetail d : alloc) {
            int recv = d.getQuantity();
            if (!firstShorted) { recv = d.getQuantity() - 2; firstShorted = true; }
            receipt.add(new TransferReceiptLine(d.getId(), recv, "BIN-DST"));
        }

        TransferOrder completed = transferService.completeTransferReceipt(created.getId(), receipt);
        assertThat(completed.getStatus()).isEqualTo(TransferOrder.Status.COMPLETED);

        // Kho đích chỉ được cộng đúng số NHẬN THỰC TẾ = 13 (không phải 15 đã xuất).
        Inventory destInv = inventoryRepository
                .findByProductIdAndWarehouseId(PROD, DST).orElseThrow();
        assertThat(destInv.getQuantity()).isEqualTo(13);

        // Có ít nhất một dòng ghi nhận hao hụt (đã xuất > đã nhận).
        int totalReceived = completed.getDetails().stream()
                .mapToInt(TransferOrderDetail::getQuantityReceived).sum();
        assertThat(totalReceived).isEqualTo(13);
        assertThat(completed.getDetails())
                .anySatisfy(d -> assertThat(d.getQuantityReceived()).isLessThan(d.getQuantity()));
    }
}
