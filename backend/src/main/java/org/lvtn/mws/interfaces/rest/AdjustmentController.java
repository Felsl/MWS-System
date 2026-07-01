package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.adjustment.ApproveAdjustmentVoucherUseCase;
import org.lvtn.mws.application.usecases.adjustment.GetAdjustmentVoucherByIdUseCase;
import org.lvtn.mws.application.usecases.adjustment.GetAllAdjustmentVouchersUseCase;
import org.lvtn.mws.infrastructure.security.UserPrincipal;
import org.lvtn.mws.interfaces.dto.response.adjustment.AdjustmentVoucherResponse;
import org.lvtn.mws.interfaces.mapper.AdjustmentWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * API Phiếu điều chỉnh tồn kho (Giai đoạn 6). Chỉ cần authenticated().
 * Thẩm quyền duyệt theo ngưỡng % được kiểm bằng code trong use case (đọc authorities từ token).
 */
@RestController
@RequestMapping("/api/v1/adjustment-vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADJUSTMENT_VIEW')")
public class AdjustmentController {

    private final ApproveAdjustmentVoucherUseCase approveUseCase;
    private final GetAdjustmentVoucherByIdUseCase getByIdUseCase;
    private final GetAllAdjustmentVouchersUseCase getAllUseCase;
    private final AdjustmentWebMapper webMapper;

    @GetMapping
    public ResponseEntity<List<AdjustmentVoucherResponse>> list() {
        return ResponseEntity.ok(webMapper.toResponseList(getAllUseCase.execute()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdjustmentVoucherResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(webMapper.toResponse(getByIdUseCase.execute(id)));
    }

    @PreAuthorize("hasAuthority('ADJUSTMENT_APPROVE')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<AdjustmentVoucherResponse> approve(@PathVariable String id,
                                                             Authentication authentication) {
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(webMapper.toResponse(
                approveUseCase.execute(id, authorities, currentUserId(authentication))));
    }

    private String currentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        return authentication.getName();
    }
}
