package org.lvtn.mws.application.usecases.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserDomainService userDomainService;

    @Transactional
    public void execute(String id) {
        userDomainService.delete(id);
    }
}
