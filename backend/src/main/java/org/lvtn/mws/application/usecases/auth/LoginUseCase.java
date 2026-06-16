package org.lvtn.mws.application.usecases.auth;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IPasswordHasher;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginUseCase {

    private final UserDomainService     userDomainService;
    private final IRoleRepository       roleRepository;
    private final IPermissionRepository permissionRepository;
    private final IPasswordHasher       passwordHasher;
    private final ITokenProvider        tokenProvider;

    public LoginResponse execute(String username, String rawPassword) {
        User user = userDomainService.findByUsername(username);

        if (!user.isActive()) {
            throw new IllegalStateException("Account is inactive");
        }
        if (!passwordHasher.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<String> permissionCodes = permissionRepository
                .findByIds(role.getPermissionIds())
                .stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());

        String token = tokenProvider.generateToken(
                user.getId(), user.getUsername(), role.getCode(), permissionCodes);

        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getFullName(), role.getCode(), permissionCodes);
    }
}
