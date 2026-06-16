package com.luontd.authservice.application.interfaces.repository;

import com.luontd.authservice.domain.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface cho Permission repository.
 * Được định nghĩa ở Application layer — infrastructure phải implement.
 */
public interface IPermissionRepository {

    Optional<Permission> findByName(String name);

    Optional<Permission> findById(UUID id);

    List<Permission> findAll();

    Permission save(Permission permission);
}
