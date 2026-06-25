package org.lvtn.mws.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.auth.ChangePasswordUseCase;
import org.lvtn.mws.application.usecases.auth.GetCurrentUserUseCase;
import org.lvtn.mws.application.usecases.auth.LoginUseCase;
import org.lvtn.mws.application.usecases.auth.RefreshTokenUseCase;
import org.lvtn.mws.infrastructure.security.JwtTokenProvider;
import org.lvtn.mws.interfaces.dto.request.auth.ChangePasswordRequest;
import org.lvtn.mws.interfaces.dto.request.auth.LoginRequest;
import org.lvtn.mws.interfaces.dto.request.auth.RefreshTokenRequest;
import org.lvtn.mws.interfaces.dto.response.auth.LoginResponse;
import org.lvtn.mws.interfaces.dto.response.user.UserResponse;
import org.lvtn.mws.interfaces.mapper.UserWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase          loginUseCase;
    private final RefreshTokenUseCase   refreshTokenUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final JwtTokenProvider      tokenProvider;
    private final UserWebMapper         userWebMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request.getUsername(), request.getPassword()));
    }

    /** Cấp lại access token (+ refresh mới) từ refresh token. Stateless — không cần đăng nhập lại. */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenUseCase.execute(request.getRefreshToken()));
    }

    /**
     * Đăng xuất. Cơ chế hiện tại STATELESS: server không lưu token nên việc đăng xuất
     * thực chất là client tự xoá access/refresh token. Endpoint này trả 200 để client
     * xác nhận và dọn token. (Sẽ thu hồi được phía server khi có bảng lưu refresh token.)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    /** Đổi mật khẩu cho người dùng đang đăng nhập (xác định qua access token). */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String bearer,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        String userId = tokenProvider.extractUserId(bearer.substring(7));
        changePasswordUseCase.execute(userId, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader("Authorization") String bearer) {
        String token  = bearer.substring(7);
        String userId = tokenProvider.extractUserId(token);
        return ResponseEntity.ok(userWebMapper.toResponse(getCurrentUserUseCase.execute(userId)));
    }
}
