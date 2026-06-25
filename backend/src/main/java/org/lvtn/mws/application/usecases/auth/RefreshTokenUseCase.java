package org.lvtn.mws.application.usecases.auth;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.ITokenProvider;
import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IPermissionRepository;
import org.lvtn.mws.domain.repository.IRoleRepository;
import org.lvtn.mws.domain.service.UserDomainService;
import org.lvtn.mws.interfaces.dto.response.auth.LoginResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Cấp lại access token từ refresh token (cơ chế STATELESS).
 * Vì stateless nên không thu hồi được refresh token trước hạn — đây là hạn chế đã biết,
 * sẽ khắc phục khi chuyển sang lưu refresh token trong DB (xem CHANGELOG, mục #6 follow-up).
 *
 * Mỗi lần refresh sẽ tải lại role + permissions mới nhất từ DB, nên thay đổi quyền
 * sẽ phản ánh vào access token kế tiếp. Có xoay vòng (rotate) refresh token.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenUseCase {

    private final UserDomainService     userDomainService;
    private final IRoleRepository       roleRepository;
    private final IPermissionRepository permissionRepository;
    private final ITokenProvider        tokenProvider;

    public LoginResponse execute(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
        if (!"refresh".equals(tokenProvider.extractTokenType(refreshToken))) {
            throw new IllegalArgumentException("Token cung cấp không phải refresh token");
        }

        String userId = tokenProvider.extractUserId(refreshToken);
        User user = userDomainService.findById(userId);
        if (!user.isActive()) {
            throw new IllegalStateException("Account is inactive");
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<String> permissionCodes = permissionRepository
                .findByIds(role.getPermissionIds())
                .stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());

        String newAccess  = tokenProvider.generateToken(
                user.getId(), user.getUsername(), role.getCode(), permissionCodes);
        String newRefresh = tokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        return new LoginResponse(newAccess, newRefresh, user.getId(), user.getUsername(),
                user.getFullName(), role.getCode(), permissionCodes);
    }
}
