package org.lvtn.mws.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.model.TransferReceiptLine;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.ICarrierRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TẦNG 3 — Luồng điều chuyển kho trên MySQL thật (INCREMENT 1).
 *
 * <p>Kiểm chứng chặng đầu: lập phiếu -> gửi duyệt (giữ chỗ tồn nguồn) -> duyệt (phân bổ
 * lô theo FEFO). Chặng xuất kho/nhận kho (trừ nguồn, cộng đích) sẽ bổ sung ở increment 2.
 *
 * @Transactional: mỗi test tự rollback.
 */
@Transactional
class TransferFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String SRC = "WH-SRC-T";
    private static final String DST = "WH-DST-T";
    private static final String PROD = "P-TRANSFER-1";

    @Autowired
    TransferOrderDomainService transferService;
    @Autowired
    IInventoryRepository inventoryRepository;
    @Autowired
    IInventoryBatchRepository batchRepository;
    @Autowired
    ICarrierRepository carrierRepository;
    @PersistenceContext
    EntityManager em;

    private void seedWarehouse(String id, String code) {
        em.createNativeQuery(
                "INSERT INTO warehouses (id, code, name, address) VALUES (?, ?, ?, ?)")
                .setParameter(1, id)
                .setParameter(2, code)
                .setParameter(3, "Kho " + code)
                .setParameter(4, "Địa chỉ " + code)
                .executeUpdate();
    }

    private void seedCarrier(String id, String code) {
        carrierRepository.save(new Carrier.Builder()
                .id(id).code(code).name("Đơn vị " + code)
                .shippingFeeRule("{\"baseFee\":20000,\"perUnitFee\":1500}")
                .status(Carrier.Status.ACTIVE).build());
    }

    private InventoryBatch batch(String id, String bin, int qty, LocalDate expiry) {
        return new InventoryBatch.Builder()
                .id(id).productId(PROD).warehouseId(SRC).binLocationId(bin)
                .batchNumber("LOT-" + id).quantity(qty).expiryDate(expiry)
                .status(InventoryBatch.Status.ACTIVE).createdAt(LocalDateTime.now()).version(0)
                .build();
    }

    @Test
    @DisplayName("Lập -> gửi duyệt (reserve nguồn) -> duyệt (FEFO chia lô theo hạn dùng)")
    void createRequestApprove_allocatesBatchesFefo() {
        // Seed 2 kho + tồn nguồn 100 + 2 lô: A hết hạn sớm (30), B hết hạn muộn (100).
        seedWarehouse(SRC, "SRC-T");
        seedWarehouse(DST, "DST-T");
        em.flush();

        inventoryRepository.save(new Inventory.Builder()
                .productId(PROD).warehouseId(SRC).quantity(100).reservedQuantity(0).version(0).build());
        batchRepository.save(batch("BATCH-A", "BIN-A", 30, LocalDate.now().plusDays(10)));
        batchRepository.save(batch("BATCH-B", "BIN-B", 100, LocalDate.now().plusDays(60)));

        // Lập phiếu điều chuyển 40 đơn vị SRC -> DST
        TransferOrder created = transferService.createTransferOrder(
                SRC, DST, "u1", List.of(new NewTransferLine(PROD, 40)));
        assertThat(created.getStatus()).isEqualTo(TransferOrder.Status.DRAFT);

        // Gửi duyệt: giữ chỗ 40 tại tồn nguồn
        transferService.requestTransferApproval(created.getId());
        Inventory srcInv = inventoryRepository
                .findByProductIdAndWarehouseId(PROD, SRC).orElseThrow();
        assertThat(srcInv.getReservedQuantity()).isEqualTo(40);
        assertThat(srcInv.availableQuantity()).isEqualTo(60);

        // Duyệt: FEFO lấy 30 từ lô A (hạn sớm) + 10 từ lô B = 40
        TransferOrder approved = transferService.approveTransferOrder(created.getId(), "boss");
        assertThat(approved.getStatus()).isEqualTo(TransferOrder.Status.APPROVED);

        List<TransferOrderDetail> lines = approved.getDetails();
        assertThat(lines).allSatisfy(d -> assertThat(d.getBatchId()).isNotBlank());
        assertThat(lines.stream().mapToInt(TransferOrderDetail::getQuantity).sum()).isEqualTo(40);

        int fromA = lines.stream().filter(d -> "BATCH-A".equals(d.getBatchId()))
                .mapToInt(TransferOrderDetail::getQuantity).sum();
        int fromB = lines.stream().filter(d -> "BATCH-B".equals(d.getBatchId()))
                .mapToInt(TransferOrderDetail::getQuantity).sum();
        assertThat(fromA).isEqualTo(30); // lô hết hạn sớm dùng trước (FEFO)
        assertThat(fromB).isEqualTo(10);
    }

    @Test
    @DisplayName("Xuất kho (dispatch) + nhận kho (receipt): tồn nguồn giảm 40, tồn đích tăng 40")
    void fullFlow_dispatchThenReceipt_movesStockFromSourceToDest() {
        seedWarehouse(SRC, "SRC-T2");
        seedWarehouse(DST, "DST-T2");
        em.flush();
        seedCarrier("CARRIER-1", "GHN");
        inventoryRepository.save(new Inventory.Builder()
                .productId(PROD).warehouseId(SRC).quantity(100).reservedQuantity(0).version(0).build());
        batchRepository.save(batch("BATCH-A2", "BIN-A", 30, LocalDate.now().plusDays(10)));
        batchRepository.save(batch("BATCH-B2", "BIN-B", 100, LocalDate.now().plusDays(60)));

        TransferOrder created = transferService.createTransferOrder(
                SRC, DST, "u1", List.of(new NewTransferLine(PROD, 40)));
        transferService.requestTransferApproval(created.getId());
        TransferOrder approved = transferService.approveTransferOrder(created.getId(), "boss");

        // Xuất kho: APPROVED -> IN_TRANSIT, trừ tồn nguồn (commitDeduction) + trừ lô nguồn
        transferService.dispatchTransferShipment(created.getId(), "CARRIER-1");
        Inventory srcAfterDispatch = inventoryRepository
                .findByProductIdAndWarehouseId(PROD, SRC).orElseThrow();
        assertThat(srcAfterDispatch.getQuantity()).isEqualTo(60);       // 100 - 40
        assertThat(srcAfterDispatch.getReservedQuantity()).isEqualTo(0); // reserved đã giải phóng khi commit

        // Nhận kho: nhận đủ 40 vào ô kệ đích BIN-DST
        List<TransferReceiptLine> receipt = approved.getDetails().stream()
                .map(d -> new TransferReceiptLine(d.getId(), d.getQuantity(), "BIN-DST"))
                .toList();
        TransferOrder completed = transferService.completeTransferReceipt(created.getId(), receipt);

        // IN_TRANSIT -> COMPLETED; tồn kho đích được tạo/cộng đúng 40
        assertThat(completed.getStatus()).isEqualTo(TransferOrder.Status.COMPLETED);
        Inventory destInv = inventoryRepository
                .findByProductIdAndWarehouseId(PROD, DST).orElseThrow();
        assertThat(destInv.getQuantity()).isEqualTo(40);
    }
}
