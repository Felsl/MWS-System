package org.lvtn.mws.application.usecases.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentApprovalPolicy;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Duyệt phiếu điều chỉnh: dựng chính sách phân tầng từ application.properties, kiểm tra
 * thẩm quyền theo % chênh lệch, áp tồn kho. Có retry optimistic-lock (Inventory có @Version)
 * theo đúng mẫu CommitStockDeductionUseCase.
 */
@Service
@RequiredArgsConstructor
public class ApproveAdjustmentVoucherUseCase {

    private final AdjustmentDomainService adjustmentDomainService;

    @Value("${stocktake.adjustment.tier2-threshold:10}")  private double tier2Threshold;
    @Value("${stocktake.adjustment.tier3-threshold:15}")  private double tier3Threshold;
    @Value("${stocktake.adjustment.tier4-threshold:20}")  private double tier4Threshold;
    @Value("${stocktake.adjustment.tier1-authority:}")               private String tier1Authority;
    @Value("${stocktake.adjustment.tier2-authority:STOCKTAKE_APPROVE}") private String tier2Authority;
    @Value("${stocktake.adjustment.tier3-authority:STOCKTAKE_APPROVE}") private String tier3Authority;
    @Value("${stocktake.adjustment.tier4-authority:ADMIN}")          private String tier4Authority;

    @Transactional
    public AdjustmentVoucher execute(String voucherId, Set<String> approverAuthorities, String approvedBy) {
        AdjustmentApprovalPolicy policy = new AdjustmentApprovalPolicy(
                tier2Threshold, tier3Threshold, tier4Threshold,
                tier1Authority, tier2Authority, tier3Authority, tier4Authority);

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return adjustmentDomainService.approveAdjustmentVoucher(
                        voucherId, approverAuthorities, approvedBy, policy);
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == maxAttempts) throw ex;
                try {
                    Thread.sleep(100L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        // Không bao giờ tới đây (vòng lặp luôn return hoặc ném).
        throw new IllegalStateException("Không thể duyệt phiếu điều chỉnh sau " + maxAttempts + " lần thử");
    }
}
