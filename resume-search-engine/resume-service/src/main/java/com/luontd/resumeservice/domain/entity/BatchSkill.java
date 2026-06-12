package com.luontd.resumeservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "batch_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchSkill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ResumeBatch batch;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName; // Tên kỹ năng (VD: Java, Spring Boot, MySQL)

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Integer weight = 1; // Trọng số từ 1 đến 5. (1: Ít quan trọng -> 5: Bắt buộc phải có)

    // (Tùy chọn) Bạn có thể thêm cột đánh dấu kỹ năng này là "Bắt buộc" hay "Điểm cộng"
    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = false;
}