package org.lvtn.mws.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final String status;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String username, String password, String status, List<String> permissions) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getId() { return id; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !"LOCKED".equals(status); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return "ACTIVE".equals(status); }
}
