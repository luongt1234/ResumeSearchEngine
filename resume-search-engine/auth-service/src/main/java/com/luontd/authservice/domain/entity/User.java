package com.luontd.authservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255) // Độ dài lớn để lưu mật khẩu đã hash (Bcrypt/Argon2)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    // Quan hệ Nhiều - Nhiều với Role (Lazy loading để tối ưu hiệu năng)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", columnDefinition = "VARCHAR(36)"),
            inverseJoinColumns = @JoinColumn(name = "role_id", columnDefinition = "VARCHAR(36)")
    )
    private Set<Role> roles;
}