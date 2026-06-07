package com.luontd.authservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name; // Ví dụ: "cv:upload", "cv:delete"

    @Column(length = 255)
    private String description;

    // Inverse side của quan hệ Role - Permission
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;
}