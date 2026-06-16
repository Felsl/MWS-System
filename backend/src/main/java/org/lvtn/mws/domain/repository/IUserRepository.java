package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    List<User> findAllActive();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailExcludingId(String id, String email);
}
