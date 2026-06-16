package org.lvtn.mws.application.usecases.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IIdGenerator;
import org.lvtn.mws.application.ports.IPasswordHasher;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserDomainService userDomainService;
    private final IPasswordHasher   passwordHasher;
    private final IIdGenerator      idGenerator;

    @Transactional
    public User execute(String username, String rawPassword,
                        String fullName, String email, String phone, String roleId) {
        String encodedPassword = passwordHasher.encode(rawPassword);
        return userDomainService.create(idGenerator.generate(), username,
                encodedPassword, fullName, email, phone, roleId);
    }
}
