package org.lvtn.mws.domain.model;

/**
 * Chính sách phân tầng thẩm quyền duyệt phiếu điều chỉnh theo % chênh lệch.
 * Được dựng ở tầng UseCase từ application.properties rồi truyền vào domain service
 * (giữ domain thuần Java, không phụ thuộc Spring).
 *
 * Tầng dựa trên mức chênh lệch lớn nhất của phiếu (maxVariancePercent):
 *   pct <= tier2Threshold              -> cần tier1Authority (mặc định: rỗng = ai cũng duyệt được)
 *   tier2Threshold < pct <= tier3Threshold -> cần tier2Authority
 *   tier3Threshold < pct <= tier4Threshold -> cần tier3Authority
 *   pct > tier4Threshold               -> cần tier4Authority (mặc định: ADMIN)
 *
 * Authority rỗng/null nghĩa là tầng đó không yêu cầu quyền đặc biệt.
 */
public class AdjustmentApprovalPolicy {

    private final double tier2Threshold;
    private final double tier3Threshold;
    private final double tier4Threshold;
    private final String tier1Authority;
    private final String tier2Authority;
    private final String tier3Authority;
    private final String tier4Authority;

    public AdjustmentApprovalPolicy(double tier2Threshold, double tier3Threshold, double tier4Threshold,
                                    String tier1Authority, String tier2Authority,
                                    String tier3Authority, String tier4Authority) {
        this.tier2Threshold = tier2Threshold;
        this.tier3Threshold = tier3Threshold;
        this.tier4Threshold = tier4Threshold;
        this.tier1Authority = tier1Authority;
        this.tier2Authority = tier2Authority;
        this.tier3Authority = tier3Authority;
        this.tier4Authority = tier4Authority;
    }

    /** Trả về authority bắt buộc cho mức chênh lệch đã cho (null/rỗng = không yêu cầu). */
    public String requiredAuthorityFor(double variancePercent) {
        if (variancePercent <= tier2Threshold) return tier1Authority;
        if (variancePercent <= tier3Threshold) return tier2Authority;
        if (variancePercent <= tier4Threshold) return tier3Authority;
        return tier4Authority;
    }
}
