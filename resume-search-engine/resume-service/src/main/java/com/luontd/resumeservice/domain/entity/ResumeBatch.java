package com.luontd.resumeservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeBatch extends BaseEntity {

    @Column(name = "batch_name", nullable = false, length = 150)
    private String batchName;

    @Column(name = "target_position", length = 100)
    private String targetPosition;

    @Column(name = "min_yoe", columnDefinition = "DECIMAL(4,1)")
    private Double minYoe;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- MỐI QUAN HỆ VỚI CV (1 Lô có nhiều CV) ---
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Resume> resumes = new ArrayList<>();

    // --- CẬP NHẬT MỚI: MỐI QUAN HỆ VỚI KỸ NĂNG (1 Lô có nhiều cấu hình Kỹ năng) ---
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BatchSkill> requiredSkills = new ArrayList<>();

    // Helper method cho CV
    public void addResume(Resume resume) {
        this.resumes.add(resume);
        resume.setBatch(this);
    }

    // Helper method cho Kỹ năng yêu cầu
    public void addRequiredSkill(BatchSkill skill) {
        this.requiredSkills.add(skill);
        skill.setBatch(this);
    }
}