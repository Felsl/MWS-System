package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import org.lvtn.mws.domain.model.PurchaseOrderLineCommand;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IPurchaseOrderDetailRepository;
import org.lvtn.mws.domain.repository.IPurchaseOrderRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain service for the purchase-order workflow. Pure Java — no framework imports.
 * Orchestration/transaction boundaries live in the UseCase layer.
 */
public class PurchaseOrderDomainService {

    private final IPurchaseOrderRepository poRepository;
    private final IPurchaseOrderDetailRepository poDetailRepository;
    private final IIdGenerator idGenerator;

    public PurchaseOrderDomainService(IPurchaseOrderRepository poRepository,
                                      IPurchaseOrderDetailRepository poDetailRepository,
                                      IIdGenerator idGenerator) {
        this.poRepository       = poRepository;
        this.poDetailRepository = poDetailRepository;
        this.idGenerator        = idGenerator;
    }

    // ── Read ───────────────────────────────────────────────────────────────

    public PurchaseOrder findById(String id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mua: " + id));
    }

    public List<PurchaseOrder> findAll() {
        return poRepository.findAll();
    }

    public List<PurchaseOrderDetail> findDetails(String poId) {
        return poDetailRepository.findByPoId(poId);
    }

    // ── Create ───────────────────────────────────────────────────────────────

    public PurchaseOrder create(String supplierId, String warehouseId, java.time.LocalDate expectedDate,
                                String createdBy, List<PurchaseOrderLineCommand> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Đơn mua phải có ít nhất một dòng hàng");
        }
        String poId = idGenerator.generate();
        PurchaseOrder po = PurchaseOrder.builder()
                .id(poId)
                .poNumber(generatePoNumber())
                .supplierId(supplierId)
                .warehouseId(warehouseId)
                .expectedDate(expectedDate)
                .createdBy(createdBy)
                .status(PurchaseOrder.Status.DRAFT)
                .build();
        PurchaseOrder saved = poRepository.save(po);

        List<PurchaseOrderDetail> details = new ArrayList<>();
        for (PurchaseOrderLineCommand line : lines) {
            details.add(PurchaseOrderDetail.builder()
                    .id(idGenerator.generate())
                    .poId(poId)
                    .productId(line.getProductId())
                    .quantityOrdered(line.getQuantityOrdered())
                    .quantityReceived(0)
                    .unitPrice(line.getUnitPrice())
                    .build());
        }
        poDetailRepository.saveAll(details);
        return saved;
    }

    // ── Workflow transitions ───────────────────────────────────────────────

    public PurchaseOrder submitForReview(String poId) {
        PurchaseOrder po = findById(poId);
        po.submitForReview();
        return poRepository.save(po);
    }

    public PurchaseOrder submitForApproval(String poId) {
        PurchaseOrder po = findById(poId);
        po.submitForApproval();
        return poRepository.save(po);
    }

    /**
     * Approve a PO. Authority check (INBOUND_APPROVE_PO) is enforced at the
     * controller via @PreAuthorize; here we only record the approver.
     */
    public PurchaseOrder approve(String poId, String approvedBy) {
        PurchaseOrder po = findById(poId);
        po.approve(approvedBy);
        return poRepository.save(po);
    }

    public PurchaseOrder reject(String poId) {
        PurchaseOrder po = findById(poId);
        po.reject();
        return poRepository.save(po);
    }

    private String generatePoNumber() {
        String number;
        do {
            number = "PO-" + idGenerator.generate();
        } while (poRepository.existsByPoNumber(number));
        return number;
    }
}
