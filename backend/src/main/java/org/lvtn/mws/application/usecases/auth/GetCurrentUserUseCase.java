package org.lvtn.mws.application.usecases.auth;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCurrentUserUseCase {

    private final UserDomainService userDomainService;

    public User execute(String userId) {
        return userDomainService.findById(userId);
    }
}
