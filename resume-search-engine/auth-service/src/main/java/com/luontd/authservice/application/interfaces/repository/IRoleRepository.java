package com.luontd.authservice.application.interfaces.repository;

import com.luontd.authservice.domain.entity.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface cho Role repository.
 * Được định nghĩa ở Application layer — infrastructure phải implement.
 */
public interface IRoleRepository {

    Optional<Role> findByName(String name);

    Optional<Role> findById(UUID id);

    Optional<List<Role>> findByUserId(UUID userId);

    List<Role> findAll();

    Role save(Role role);
}
