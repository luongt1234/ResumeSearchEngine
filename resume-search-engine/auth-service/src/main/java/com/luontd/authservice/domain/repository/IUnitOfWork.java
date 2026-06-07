package com.luontd.authservice.domain.repository;

import com.luontd.authservice.domain.entity.Permission;
import com.luontd.authservice.domain.entity.Role;
import com.luontd.authservice.domain.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Unit of Work pattern — gom toàn bộ repository operations trong một transaction.
 * Thay thế việc inject trực tiếp nhiều JpaRepository vào service.
 */
public interface IUnitOfWork {

    // ── User ──────────────────────────────────────────────
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserById(UUID id);

    /** Lấy User kèm danh sách roles trong 1 query — tránh LazyInitializationException */
    Optional<User> findUserByUsernameWithRoles(String username);
    Optional<User> findUserByIdWithRoles(UUID id);

    User saveUser(User user);
    void deleteUser(UUID id);

    // ── Role ──────────────────────────────────────────────
    Optional<Role> findRoleByName(String name);
    Optional<Role> findRoleById(UUID id);
    List<Role> findAllRoles();
    Role saveRole(Role role);

    // ── Permission ────────────────────────────────────────
    Optional<Permission> findPermissionByName(String name);
    Optional<Permission> findPermissionById(UUID id);
    List<Permission> findAllPermissions();
    Permission savePermission(Permission permission);

    // ── Transaction ───────────────────────────────────────
    /**
     * Flush toàn bộ thay đổi pending xuống DB trong transaction hiện tại.
     * Tương đương SaveChanges() trong Entity Framework.
     */
    void saveChanges();
}
