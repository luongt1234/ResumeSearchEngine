package com.luontd.authservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name; // Bắt buộc định dạng dạng: ROLE_ADMIN, ROLE_CANDIDATE

    @Column(length = 255)
    private String description;

    // Quan hệ Nhiều - Nhiều với Permission
    @ManyToMany(fetch = FetchType.EAGER) // Eager để khi lấy Role sẽ lấy kèm bộ quyền luôn
    @JoinTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}