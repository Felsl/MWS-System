package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IRoleRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;

import java.util.List;

public class UserDomainService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final IWarehouseRepository warehouseRepository;

    public UserDomainService(IUserRepository userRepository,
                             IRoleRepository roleRepository,
                             IWarehouseRepository warehouseRepository) {
        this.userRepository      = userRepository;
        this.roleRepository      = roleRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public User create(String id, String username, String encodedPassword,
                       String fullName, String email, String phone, String roleId) {
        validateRoleExists(roleId);
        validateUsernameNotTaken(username);
        if (email != null && !email.isBlank()) validateEmailNotTaken(email);

        User user = new User.UserBuilder()
                .id(id)
                .username(username)
                .password(encodedPassword)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .roleId(roleId)
                .build();

        return userRepository.save(user);
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public List<User> findAllActive() {
        return userRepository.findAllActive();
    }

    public User update(String id, String fullName, String email, String phone, String roleId) {
        validateRoleExists(roleId);
        User user = findById(id);
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            validateEmailNotTakenExcluding(id, email);
        }
        user.update(fullName, email, phone, roleId);
        return userRepository.save(user);
    }

    public void delete(String id) {
        User user = findById(id);
        user.delete();
        userRepository.save(user);
    }

    public void validateWarehouseExists(String warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new RuntimeException("Warehouse not found: " + warehouseId);
        }
    }

    private void validateRoleExists(String roleId) {
        roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
    }

    private void validateUsernameNotTaken(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    private void validateEmailNotTaken(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }
    }

    private void validateEmailNotTakenExcluding(String id, String email) {
        if (userRepository.existsByEmailExcludingId(id, email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }
    }
}
