package org.lvtn.mws.application.usecases.auth;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IPasswordHasher;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Đổi mật khẩu cho người dùng đang đăng nhập: xác minh mật khẩu cũ rồi đặt mật khẩu mới.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChangePasswordUseCase {

    private final UserDomainService userDomainService;
    private final IPasswordHasher   passwordHasher;

    public void execute(String userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        User user = userDomainService.findById(userId);
        if (!passwordHasher.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        if (passwordHasher.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
        }
        userDomainService.changePassword(userId, passwordHasher.encode(newPassword));
    }
}
