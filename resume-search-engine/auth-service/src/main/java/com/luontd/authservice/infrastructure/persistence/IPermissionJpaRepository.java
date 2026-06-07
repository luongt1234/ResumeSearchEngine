package com.luontd.authservice.infrastructure.persistence;

import com.luontd.authservice.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IPermissionJpaRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);
}
