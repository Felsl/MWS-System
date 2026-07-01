package org.lvtn.mws.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IPermissionRepository;
import org.lvtn.mws.domain.repository.IRoleRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserRepository       userRepository;
    private final IRoleRepository       roleRepository;
    private final IPermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new UsernameNotFoundException("Role not found for user: " + username));

        List<String> permissions = permissionRepository
                .findByIds(role.getPermissionIds())
                .stream()
                .map(Permission::getCode)
                .collect(Collectors.toCollection(java.util.ArrayList::new));

        permissions.add("ROLE_" + role.getCode());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                permissions);
    }
}
