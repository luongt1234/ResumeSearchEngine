package com.luontd.authservice.infrastructure.persistence;

import com.luontd.authservice.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRoleJpaRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
    Optional<List<Role>> findByUsers_Id(UUID userId);
}
