package com.luontd.authservice.infrastructure.persistence.impl;

import com.luontd.authservice.application.interfaces.repository.IRoleRepository;
import com.luontd.authservice.domain.entity.Role;
import com.luontd.authservice.infrastructure.persistence.IRoleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation của IRoleRepository.
 * Delegate sang Spring Data JPA repository.
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements IRoleRepository {

    private final IRoleJpaRepository jpaRepository;

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<List<Role>> findByUserId(UUID userId) {
        return jpaRepository.findByUsers_Id(userId);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Role save(Role role) {
        return jpaRepository.save(role);
    }
}
