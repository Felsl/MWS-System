package org.lvtn.mws.infrastructure.persistence.repository.user;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.UserInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserInfraMapper   mapper;

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(String id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsernameAndDeletedAtIsNull(username).map(mapper::toDomain);
    }

    @Override
    public List<User> findAllActive() {
        return jpaRepository.findAllByDeletedAtIsNull()
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsernameAndDeletedAtIsNull(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    @Override
    public boolean existsByEmailExcludingId(String id, String email) {
        return jpaRepository.existsByEmailAndIdNotAndDeletedAtIsNull(email, id);
    }
}
