package org.lvtn.mws.application.usecases.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserDomainService userDomainService;

    @Transactional
    public User execute(String id, String fullName, String email, String phone, String roleId) {
        return userDomainService.update(id, fullName, email, phone, roleId);
    }
}
