package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IStocktakeDetailRepository;
import org.lvtn.mws.domain.repository.IStocktakeSessionRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Nghiệp vụ Kiểm kê kho (Giai đoạn 6) — thuần Java. Ranh giới @Transactional/retry
 * được áp ở tầng UseCase.
 */
public class StocktakeDomainService {

    private final IStocktakeSessionRepository sessionRepository;
    private final IStocktakeDetailRepository detailRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IIdGenerator idGenerator;

    public StocktakeDomainService(IStocktakeSessionRepository sessionRepository,
                                  IStocktakeDetailRepository detailRepository,
                                  IInventoryBatchRepository batchRepository,
                                  IIdGenerator idGenerator) {
        this.sessionRepository = sessionRepository;
        this.detailRepository  = detailRepository;
        this.batchRepository   = batchRepository;
        this.idGenerator       = idGenerator;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public StocktakeSession findById(String id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên kiểm kê: " + id));
    }

    public List<StocktakeSession> findAll() { return sessionRepository.findAll(); }

    public List<StocktakeDetail> findDetails(String sessionId) {
        return detailRepository.findBySessionId(sessionId);
    }

    // ── 1. Bắt đầu kiểm kê: đóng băng kho + chụp ảnh tồn ─────────────────────

    /**
     * Tạo phiên kiểm kê FREEZED cho kho và chụp ảnh (snapshot) tồn hiện tại của TỪNG LÔ
     * tại từng vị trí thành các dòng đối chiếu (system_quantity = tồn lô tại thời điểm chụp).
     * Chèn hàng loạt (bulk insert) các dòng chi tiết.
     */
    public StocktakeSession startStocktake(String warehouseId, String createdBy) {
        if (sessionRepository.isWarehouseFrozen(warehouseId)) {
            throw new IllegalStateException("Kho " + warehouseId + " đang có phiên kiểm kê dở dang (FREEZED)");
        }

        StocktakeSession session = StocktakeSession.builder()
                .id(idGenerator.generate())
                .warehouseId(warehouseId)
                .status(StocktakeSession.Status.FREEZED)
                .freezeStartedAt(java.time.LocalDateTime.now())
                .createdBy(createdBy)
                .build();
        StocktakeSession saved = sessionRepository.save(session);

        List<InventoryBatch> batches = batchRepository.findByWarehouseId(warehouseId);
        List<StocktakeDetail> snapshot = new ArrayList<>();
        for (InventoryBatch b : batches) {
            snapshot.add(StocktakeDetail.builder()
                    .id(idGenerator.generate())
                    .sessionId(saved.getId())
                    .productId(b.getProductId())
                    .binLocationId(b.getBinLocationId())
                    .batchId(b.getId())
                    .systemQuantity(b.getQuantity())
                    .build());
        }
        if (!snapshot.isEmpty()) {
            detailRepository.saveAll(snapshot);
        }
        return saved;
    }

    // ── 2. Nhập số đếm thực tế ───────────────────────────────────────────────

    public StocktakeDetail submitCountedQuantity(String detailId, int countedQuantity, String countedBy) {
        StocktakeDetail detail = getDetail(detailId);
        StocktakeSession session = findById(detail.getSessionId());
        if (!session.isFrozen()) {
            throw new IllegalStateException("Phiên kiểm kê đã đóng, không thể nhập số đếm");
        }
        detail.submitCount(countedQuantity, countedBy);
        return detailRepository.save(detail);
    }

    // ── 3. Duyệt từng dòng (line-item approval) ──────────────────────────────

    public StocktakeDetail approveLine(String detailId, String approvedBy, String reason) {
        StocktakeDetail detail = getDetail(detailId);
        detail.approveLine(approvedBy, reason);
        return detailRepository.save(detail);
    }

    // ── 4. Hoàn tất phiên: mở băng kho ───────────────────────────────────────

    /**
     * Hoàn tất phiên kiểm kê: yêu cầu mọi dòng đã được kiểm đếm, rồi chuyển
     * FREEZED -> ADJUSTED (mở băng). Việc sinh phiếu điều chỉnh do tầng UseCase điều phối
     * (gọi AdjustmentDomainService.generateFromStocktake).
     */
    public StocktakeSession completeStocktakeSession(String sessionId) {
        StocktakeSession session = findById(sessionId);
        List<StocktakeDetail> details = detailRepository.findBySessionId(sessionId);
        boolean anyUncounted = details.stream().anyMatch(d -> !d.isCounted());
        if (anyUncounted) {
            throw new IllegalStateException("Còn vị trí/lô chưa kiểm đếm, không thể hoàn tất phiên");
        }
        session.complete();
        return sessionRepository.save(session);
    }

    private StocktakeDetail getDetail(String detailId) {
        return detailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng kiểm kê: " + detailId));
    }
}
