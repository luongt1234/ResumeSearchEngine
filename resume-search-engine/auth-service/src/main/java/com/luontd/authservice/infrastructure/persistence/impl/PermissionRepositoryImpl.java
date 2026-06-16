package com.luontd.authservice.infrastructure.persistence.impl;

import com.luontd.authservice.application.interfaces.repository.IPermissionRepository;
import com.luontd.authservice.domain.entity.Permission;
import com.luontd.authservice.infrastructure.persistence.IPermissionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation của IPermissionRepository.
 * Delegate sang Spring Data JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class PermissionRepositoryImpl implements IPermissionRepository {

    private final IPermissionJpaRepository jpaRepository;

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Permission> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Permission save(Permission permission) {
        return jpaRepository.save(permission);
    }
}
