package com.luontd.resumeservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, unique = true) // UNIQUE để ép quan hệ 1-1
    private Resume resume;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "DECIMAL(4,1)")
    private Double yoe; // Years of Experience (Ví dụ: 3.5 năm)
}