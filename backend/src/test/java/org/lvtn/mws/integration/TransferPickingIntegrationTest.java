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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TẦNG 3 — PICKING CHO ĐIỀU CHUYỂN KHO trên MySQL thật (khoá hồi quy INC1..INC3b).
 *
 * <p>Bộ test này chốt các nhánh nghiệp vụ then chốt mà backend xử lý riêng cho điều chuyển
 * (khác hẳn picking của đơn xuất SO):
 * <ol>
 *   <li><b>FEFO chỉ GỢI Ý</b>: một dòng cần 15 được tách thành 2 lô (10 lô cận hạn + 5 lô sau);
 *       khi xuất kho trừ đúng LÔ THỰC NHẶT (không dùng lô FEFO gán lúc duyệt — vì INC3 đã bỏ FEFO ở approve).</li>
 *   <li><b>Quét lô khác gợi ý</b>: dòng gợi ý (không khoá) cho phép picker quét lô khác; hệ thống ghi
 *       lô thực quét, xuất kho trừ đúng lô đó (chứng minh không ép FEFO cho điều chuyển).</li>
 *   <li><b>Dòng CHỈ ĐỊNH lô</b>: quét sai lô bị chặn; quét đúng lô mới cho qua.</li>
 *   <li><b>Guard</b>: chưa gom hàng xong thì không được xuất kho.</li>
 * </ol>
 *
 * <p>Cách seed bám đúng {@code TransferFlowIntegrationTest} có sẵn (schema test do Hibernate
 * {@code create-drop} sinh từ entity nên không ràng buộc FK product/bin — chỉ seed warehouse + tồn + lô + carrier).
 *
 * <p>{@code @Transactional}: mỗi test tự rollback. Yêu cầu: máy chạy test phải có Docker.
 */
@Transactional
class TransferPickingIntegrationTest extends AbstractIntegrationTest {

    private static final String SRC = "WH-SRC-TP";
    private static final String DST = "WH-DST-TP";
    private static final String PROD = "P-TP-1";
    private static final String CARRIER = "CARRIER-TP";

    @Autowired TransferOrderDomainService transferService;
    @Autowired TransferPickingDomainService pickingService;
    @Autowired IInventoryRepository inventoryRepository;
    @Autowired IInventoryBatchRepository batchRepository;
    @Autowired IPickingListRepository pickingRepository;
    @Autowired ICarrierRepository carrierRepository;
    @PersistenceContext EntityManager em;

    // ─────────────────────────────── helpers seed ───────────────────────────────

    private void seedWarehouse(String id, String code) {
        em.createNativeQuery(
                "INSERT INTO warehouses (id, code, name, address) VALUES (?, ?, ?, ?)")
                .setParameter(1, id).setParameter(2, code)
                .setParameter(3, "Kho " + code).setParameter(4, "Địa chỉ " + code)
                .executeUpdate();
    }

    private void seedCarrier() {
        carrierRepository.save(new Carrier.Builder()
                .id(CARRIER).code("GHN-TP").name("Đơn vị GHN-TP")
                .shippingFeeRule("{\"baseFee\":20000,\"perUnitFee\":1500}")
                .status(Carrier.Status.ACTIVE).build());
    }

    private void seedInventory(int qty) {
        inventoryRepository.save(new Inventory.Builder()
                .productId(PROD).warehouseId(SRC).quantity(qty).reservedQuantity(0).version(0).build());
    }

    /** Tạo 1 lô ACTIVE ở kho nguồn. expiryDays nhỏ hơn => cận hạn hơn (FEFO ưu tiên). */
    private InventoryBatch seedBatch(String id, String bin, String number, int qty, int expiryDays) {
        InventoryBatch b = new InventoryBatch.Builder()
                .id(id).productId(PROD).warehouseId(SRC).binLocationId(bin)
                .batchNumber(number).quantity(qty).expiryDate(LocalDate.now().plusDays(expiryDays))
                .status(InventoryBatch.Status.ACTIVE).createdAt(LocalDateTime.now()).version(0)
                .build();
        return batchRepository.save(b);
    }

    /** create -> request(reserve) -> approve. Trả phiếu ở trạng thái APPROVED (chưa gom hàng). */
    private TransferOrder approvedTransfer(List<NewTransferLine> lines) {
        TransferOrder created = transferService.createTransferOrder(SRC, DST, "u1", lines);
        assertThat(created.getStatus()).isEqualTo(TransferOrder.Status.DRAFT);
        transferService.requestTransferApproval(created.getId());
        TransferOrder approved = transferService.approveTransferOrder(created.getId(), "boss");
        assertThat(approved.getStatus()).isEqualTo(TransferOrder.Status.APPROVED);
        return approved;
    }

    private PickingListDetail detailByBatch(PickingList pl, String batchId) {
        return pl.getDetails().stream()
                .filter(d -> batchId.equals(d.getBatchId()))
                .findFirst().orElseThrow(() ->
                        new AssertionError("Không thấy dòng nhặt gợi ý lô " + batchId));
    }

    private PickingList reloadPicking(String transferId) {
        return pickingRepository.findByTransferOrderId(transferId).orElseThrow();
    }

    // ─────────────────────────────── TEST 1 ───────────────────────────────

    @Test
    @DisplayName("FEFO gợi ý tách 1 dòng thành 2 lô -> nhặt -> xuất kho trừ đúng lô thực nhặt -> nhận kho đích")
    void fefoSuggested_splitsOneLineIntoTwoBatches_fullFlow() {
        seedWarehouse(SRC, "SRC-TP1");
        seedWarehouse(DST, "DST-TP1");
        em.flush();
        seedCarrier();
        seedInventory(100);
        // Lô A cận hạn (10 ngày) tồn 10; lô B hạn xa (60 ngày) tồn 100.
        InventoryBatch a = seedBatch("TP1-A", "BIN-A", "LOT-TP1-A", 10, 10);
        InventoryBatch b = seedBatch("TP1-B", "BIN-B", "LOT-TP1-B", 100, 60);

        // Điều chuyển 15 đơn vị, KHÔNG chỉ định lô -> gợi ý FEFO.
        TransferOrder to = approvedTransfer(List.of(new NewTransferLine(PROD, 15)));

        // Sinh lệnh gom hàng: FEFO cắt 10 (lô A) + 5 (lô B) => 2 dòng nhặt.
        PickingList pl = pickingService.generateForTransfer(to.getId());
        assertThat(pl.getStatus()).isEqualTo(PickingList.Status.PENDING);
        assertThat(pl.getDetails()).hasSize(2);

        PickingListDetail lineA = detailByBatch(pl, a.getId());
        PickingListDetail lineB = detailByBatch(pl, b.getId());
        assertThat(lineA.getQuantityToPick()).isEqualTo(10);   // lô cận hạn lấy trước
        assertThat(lineB.getQuantityToPick()).isEqualTo(5);
        assertThat(lineA.getRequiredBatchId()).isNull();        // dòng gợi ý (không khoá)
        assertThat(lineB.getRequiredBatchId()).isNull();

        // Phiếu chuyển sang PICKING sau khi sinh lệnh gom.
        assertThat(transferService.findById(to.getId()).getStatus())
                .isEqualTo(TransferOrder.Status.PICKING);

        // Picker quét từng dòng bằng mã lô gợi ý.
        pickingService.confirmScanForTransfer(lineA.getId(), "LOT-TP1-A", "picker");
        pickingService.confirmScanForTransfer(lineB.getId(), "LOT-TP1-B", "picker");

        PickingList picked = reloadPicking(to.getId());
        assertThat(picked.getStatus()).isEqualTo(PickingList.Status.COMPLETED);
        assertThat(picked.getDetails()).allSatisfy(d -> {
            assertThat(d.isConfirmed()).isTrue();
            assertThat(d.getActualBatchId()).isNotBlank();
        });

        // Xuất kho: trừ tồn tổng 15 + trừ đúng lô thực nhặt (A:-10, B:-5).
        transferService.dispatchTransferShipment(to.getId(), CARRIER);

        Inventory srcInv = inventoryRepository.findByProductIdAndWarehouseId(PROD, SRC).orElseThrow();
        assertThat(srcInv.getQuantity()).isEqualTo(85);          // 100 - 15
        assertThat(srcInv.getReservedQuantity()).isEqualTo(0);   // giải phóng giữ chỗ khi commit
        assertThat(batchRepository.findById(a.getId()).orElseThrow().getQuantity()).isEqualTo(0);   // 10 - 10
        assertThat(batchRepository.findById(b.getId()).orElseThrow().getQuantity()).isEqualTo(95);  // 100 - 5

        // Phiếu IN_TRANSIT; chi tiết được dựng lại theo lô thực nhặt (mỗi lô = 1 dòng).
        TransferOrder inTransit = transferService.findById(to.getId());
        assertThat(inTransit.getStatus()).isEqualTo(TransferOrder.Status.IN_TRANSIT);
        List<TransferOrderDetail> alloc = inTransit.getDetails();
        assertThat(alloc).hasSize(2);
        assertThat(alloc).allSatisfy(d -> assertThat(d.getBatchId()).isNotBlank());
        assertThat(alloc.stream().mapToInt(TransferOrderDetail::getQuantity).sum()).isEqualTo(15);

        // Nhận kho đích đủ 15 vào ô kệ đích -> COMPLETED, tồn đích = 15.
        List<TransferReceiptLine> receipt = alloc.stream()
                .map(d -> new TransferReceiptLine(d.getId(), d.getQuantity(), "BIN-DST"))
                .toList();
        TransferOrder completed = transferService.completeTransferReceipt(to.getId(), receipt);
        assertThat(completed.getStatus()).isEqualTo(TransferOrder.Status.COMPLETED);

        Inventory dstInv = inventoryRepository.findByProductIdAndWarehouseId(PROD, DST).orElseThrow();
        assertThat(dstInv.getQuantity()).isEqualTo(15);
    }

    // ─────────────────────────────── TEST 2 ───────────────────────────────

    @Test
    @DisplayName("Dòng gợi ý FEFO: picker quét lô KHÁC gợi ý -> ghi & trừ đúng lô đã quét (không ép FEFO)")
    void fefoSuggested_pickerScansDifferentBatch_honorsScan() {
        seedWarehouse(SRC, "SRC-TP2");
        seedWarehouse(DST, "DST-TP2");
        em.flush();
        seedCarrier();
        seedInventory(100);
        // A cận hạn (FEFO sẽ gợi ý A), B hạn xa. Cả hai đều đủ tồn cho 5.
        InventoryBatch a = seedBatch("TP2-A", "BIN-A", "LOT-TP2-A", 20, 10);
        InventoryBatch b = seedBatch("TP2-B", "BIN-B", "LOT-TP2-B", 20, 60);

        TransferOrder to = approvedTransfer(List.of(new NewTransferLine(PROD, 5)));
        PickingList pl = pickingService.generateForTransfer(to.getId());

        // FEFO gợi ý đúng 1 dòng lấy 5 từ lô A (cận hạn).
        assertThat(pl.getDetails()).hasSize(1);
        PickingListDetail line = pl.getDetails().get(0);
        assertThat(line.getBatchId()).isEqualTo(a.getId());     // gợi ý là lô A
        assertThat(line.getRequiredBatchId()).isNull();         // nhưng KHÔNG khoá

        // Picker quét lô B (khác gợi ý) — hợp lệ vì dòng không khoá.
        pickingService.confirmScanForTransfer(line.getId(), "LOT-TP2-B", "picker");

        PickingListDetail after = reloadPicking(to.getId()).getDetails().get(0);
        assertThat(after.getActualBatchId()).isEqualTo(b.getId());   // ghi đúng lô THỰC quét (B)
        assertThat(reloadPicking(to.getId()).getStatus()).isEqualTo(PickingList.Status.COMPLETED);

        // Xuất kho: trừ đúng lô B (đã quét), lô A giữ nguyên.
        transferService.dispatchTransferShipment(to.getId(), CARRIER);
        assertThat(batchRepository.findById(b.getId()).orElseThrow().getQuantity()).isEqualTo(15); // 20 - 5
        assertThat(batchRepository.findById(a.getId()).orElseThrow().getQuantity()).isEqualTo(20); // giữ nguyên
        assertThat(inventoryRepository.findByProductIdAndWarehouseId(PROD, SRC).orElseThrow()
                .getQuantity()).isEqualTo(95);

        // Chi tiết phiếu dựng lại theo lô B.
        List<TransferOrderDetail> alloc = transferService.findById(to.getId()).getDetails();
        assertThat(alloc).hasSize(1);
        assertThat(alloc.get(0).getBatchId()).isEqualTo(b.getId());
    }

    // ─────────────────────────────── TEST 3 ───────────────────────────────

    @Test
    @DisplayName("Dòng CHỈ ĐỊNH lô: quét sai lô -> bị chặn (Sai lô!)")
    void designatedBatch_scanWrongBatch_rejected() {
        seedWarehouse(SRC, "SRC-TP3");
        seedWarehouse(DST, "DST-TP3");
        em.flush();
        seedInventory(100);
        seedBatch("TP3-A", "BIN-A", "LOT-TP3-A", 20, 10);
        InventoryBatch b = seedBatch("TP3-B", "BIN-B", "LOT-TP3-B", 20, 60);

        // Chỉ định lô B cho dòng điều chuyển 5 đơn vị.
        TransferOrder to = approvedTransfer(List.of(new NewTransferLine(PROD, 5, b.getId())));
        PickingList pl = pickingService.generateForTransfer(to.getId());

        assertThat(pl.getDetails()).hasSize(1);
        PickingListDetail line = pl.getDetails().get(0);
        assertThat(line.getRequiredBatchId()).isEqualTo(b.getId());  // dòng bị KHOÁ theo lô B

        // Quét lô A (sai) -> ném IllegalArgumentException.
        assertThatThrownBy(() ->
                pickingService.confirmScanForTransfer(line.getId(), "LOT-TP3-A", "picker"))
                .isInstanceOf(IllegalArgumentException.class);

        // Lệnh gom vẫn chưa hoàn tất (không có dòng nào được xác nhận).
        assertThat(reloadPicking(to.getId()).getStatus()).isNotEqualTo(PickingList.Status.COMPLETED);
    }

    // ─────────────────────────────── TEST 4 ───────────────────────────────

    @Test
    @DisplayName("Dòng CHỈ ĐỊNH lô: quét đúng lô -> nhặt xong, xuất kho trừ đúng lô chỉ định")
    void designatedBatch_scanCorrectBatch_dispatchesThatBatch() {
        seedWarehouse(SRC, "SRC-TP4");
        seedWarehouse(DST, "DST-TP4");
        em.flush();
        seedCarrier();
        seedInventory(100);
        InventoryBatch a = seedBatch("TP4-A", "BIN-A", "LOT-TP4-A", 20, 10);
        InventoryBatch b = seedBatch("TP4-B", "BIN-B", "LOT-TP4-B", 20, 60);

        TransferOrder to = approvedTransfer(List.of(new NewTransferLine(PROD, 5, b.getId())));
        PickingList pl = pickingService.generateForTransfer(to.getId());
        PickingListDetail line = pl.getDetails().get(0);

        // Quét đúng lô chỉ định B.
        pickingService.confirmScanForTransfer(line.getId(), "LOT-TP4-B", "picker");
        PickingList picked = reloadPicking(to.getId());
        assertThat(picked.getStatus()).isEqualTo(PickingList.Status.COMPLETED);
        assertThat(picked.getDetails().get(0).getActualBatchId()).isEqualTo(b.getId());

        // Xuất kho: trừ đúng lô B (lô chỉ định), lô A giữ nguyên.
        transferService.dispatchTransferShipment(to.getId(), CARRIER);
        assertThat(batchRepository.findById(b.getId()).orElseThrow().getQuantity()).isEqualTo(15);
        assertThat(batchRepository.findById(a.getId()).orElseThrow().getQuantity()).isEqualTo(20);
        assertThat(transferService.findById(to.getId()).getStatus())
                .isEqualTo(TransferOrder.Status.IN_TRANSIT);
    }

    // ─────────────────────────────── TEST 5 ───────────────────────────────

    @Test
    @DisplayName("Guard: chưa gom hàng xong thì KHÔNG được xuất kho (dispatch bị chặn)")
    void dispatch_beforePickingCompleted_throws() {
        seedWarehouse(SRC, "SRC-TP5");
        seedWarehouse(DST, "DST-TP5");
        em.flush();
        seedCarrier();
        seedInventory(100);
        seedBatch("TP5-A", "BIN-A", "LOT-TP5-A", 20, 10);

        TransferOrder to = approvedTransfer(List.of(new NewTransferLine(PROD, 5)));
        pickingService.generateForTransfer(to.getId()); // mới sinh lệnh (PENDING), chưa quét

        // Chưa nhặt xong -> xuất kho phải bị chặn.
        assertThatThrownBy(() -> transferService.dispatchTransferShipment(to.getId(), CARRIER))
                .isInstanceOf(IllegalStateException.class);

        // Tồn nguồn không đổi (chưa trừ kho).
        assertThat(inventoryRepository.findByProductIdAndWarehouseId(PROD, SRC).orElseThrow()
                .getQuantity()).isEqualTo(100);
    }
}
