package com.luontd.authservice.application.interfaces.repository;

import com.luontd.authservice.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface cho User repository.
 * Được định nghĩa ở Application layer — infrastructure phải implement.
 */
public interface IUserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findById(UUID id);
}
