package com.luontd.resumeservice.domain.entity;

import com.luontd.resumeservice.domain.enums.EtlStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId; // Giữ là String nếu ID từ user-service dạng chuỗi, hoặc đổi thành UUID nếu user-service cũng dùng UUID.

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "etl_status", nullable = false, length = 20)
    private EtlStatus etlStatus = EtlStatus.PENDING;

    // Lỗi có thể rất dài, nên dùng kiểu TEXT thay vì VARCHAR(255)
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // --- RELATIONSHIPS ---
    // 1 cv sẽ có 1 bacth skill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false) // Ép buộc mỗi CV khi up lên phải nằm trong 1 lô cụ thể
    private ResumeBatch batch;

    // 1 CV chỉ có 1 Profile (Quan hệ 1-1)
    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ResumeProfile profile;

    // 1 CV có nhiều Skills
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSkill> skills = new ArrayList<>();

    // 1 CV có nhiều Experiences
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeExperience> experiences = new ArrayList<>();

    // 1 CV có nhiều Educations
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeEducation> educations = new ArrayList<>();

    // Helper methods để set quan hệ 2 chiều (Bidirectional)
    public void setProfile(ResumeProfile profile) {
        this.profile = profile;
        profile.setResume(this);
    }

    public void addSkill(ResumeSkill skill) {
        skills.add(skill);
        skill.setResume(this);
    }

    public void addExperience(ResumeExperience experience) {
        experiences.add(experience);
        experience.setResume(this);
    }
}