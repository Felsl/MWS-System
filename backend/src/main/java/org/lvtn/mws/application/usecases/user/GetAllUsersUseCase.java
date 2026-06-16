package org.lvtn.mws.application.usecases.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.service.UserDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllUsersUseCase {

    private final UserDomainService userDomainService;

    public List<User> execute() {
        return userDomainService.findAllActive();
    }
}
