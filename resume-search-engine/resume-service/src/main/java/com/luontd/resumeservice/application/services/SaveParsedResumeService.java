package com.luontd.resumeservice.application.services;

import com.luontd.resumeservice.domain.entity.*;
import com.luontd.resumeservice.domain.enums.EtlStatus;
import com.luontd.resumeservice.infrastructure.messaging.kafka.consumer.dto.CvParsedEventDto;
import com.luontd.resumeservice.infrastructure.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service xử lý lưu CV đã parse vào MySQL.
 * Được gọi bởi CvParsedMysqlConsumer sau khi nhận Kafka event từ etl-worker.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaveParsedResumeService {

    private final IResumeJpaRepository resumeRepository;
    private final IResumeProfileJpaRepository profileRepository;
    private final IResumeSkillJpaRepository skillRepository;
    private final IResumeExperienceJpaRepository experienceRepository;
    private final IResumeEducationJpaRepository educationRepository;

    /**
     * Lưu toàn bộ dữ liệu CV đã parse vào MySQL.
     * Idempotent: xóa dữ liệu cũ trước khi ghi mới (upsert pattern).
     *
     * @param event CV parsed event từ etl-worker qua Kafka
     */
    @Transactional
    public void save(CvParsedEventDto event) {
        UUID resumeId = event.getResumeId();
        log.info("[MySQL Consumer] Bắt đầu lưu parsed CV, resumeId={}", resumeId);

        // Tìm Resume entity — phải tồn tại (được tạo khi upload)
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.error("[MySQL Consumer] Không tìm thấy Resume với id={}", resumeId);
                    return new IllegalStateException("Resume not found: " + resumeId);
                });

        // 1. Upsert ResumeProfile (thông tin cơ bản)
        saveProfile(resume, event);

        // 2. Upsert Skills — xóa cũ, ghi mới
        saveSkills(resume, event);

        // 3. Upsert Experiences — xóa cũ, ghi mới
        saveExperiences(resume, event);

        // 4. Upsert Educations — xóa cũ, ghi mới
        saveEducations(resume, event);

        // 5. Cập nhật ETL status sang COMPLETED
        resume.setEtlStatus(EtlStatus.COMPLETED);
        resumeRepository.save(resume);

        log.info("[MySQL Consumer] ✅ Lưu thành công, resumeId={}", resumeId);
    }

    @Transactional
    public void updateStatusFailed(UUID resumeId, String reason) {
        log.info("[MySQL Consumer] Cập nhật trạng thái lỗi, resumeId={}, reason={}", resumeId, reason);
        resumeRepository.findById(resumeId).ifPresent(resume -> {
            resume.setEtlStatus(EtlStatus.FAILED);
            // Có thể thêm trường error_message vào entity Resume sau này nếu muốn lưu lý do lỗi
            resumeRepository.save(resume);
        });
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void saveProfile(Resume resume, CvParsedEventDto event) {
        // Xóa profile cũ nếu tồn tại — tránh lỗi UNIQUE constraint (resume_id UNIQUE)
        profileRepository.findByResumeId(resume.getId())
                .ifPresent(profileRepository::delete);
        profileRepository.flush();

        ResumeProfile profile = ResumeProfile.builder()
                .resume(resume)
                .fullName(event.getFullName())
                .email(event.getEmail())
                .phoneNumber(event.getPhone())
                .summary(event.getSummary())
                .build();
        profileRepository.save(profile);
        log.debug("[MySQL Consumer] Đã lưu profile cho resumeId={}", resume.getId());
    }

    private void saveSkills(Resume resume, CvParsedEventDto event) {
        skillRepository.deleteAllByResumeId(resume.getId());
        skillRepository.flush();

        if (event.getSkills() == null || event.getSkills().isEmpty()) return;

        List<ResumeSkill> skills = event.getSkills().stream()
                .filter(name -> name != null && !name.isBlank())
                .map(name -> ResumeSkill.builder()
                        .resume(resume)
                        .skillName(name.trim())
                        .build())
                .collect(Collectors.toList());

        skillRepository.saveAll(skills);
        log.debug("[MySQL Consumer] Đã lưu {} skills cho resumeId={}", skills.size(), resume.getId());
    }

    private void saveExperiences(Resume resume, CvParsedEventDto event) {
        experienceRepository.deleteAllByResumeId(resume.getId());
        experienceRepository.flush();

        if (event.getExperience() == null || event.getExperience().isEmpty()) return;

        List<ResumeExperience> experiences = event.getExperience().stream()
                .map(exp -> ResumeExperience.builder()
                        .resume(resume)
                        .companyName(exp.getCompany() != null ? exp.getCompany() : "Unknown")
                        .jobTitle(exp.getTitle() != null ? exp.getTitle() : "Unknown")
                        // duration từ LLM là chuỗi tự do (vd: "2021-2023") — append vào description
                        .description(buildExperienceDescription(exp))
                        .build())
                .collect(Collectors.toList());

        experienceRepository.saveAll(experiences);
        log.debug("[MySQL Consumer] Đã lưu {} experiences cho resumeId={}", experiences.size(), resume.getId());
    }

    private void saveEducations(Resume resume, CvParsedEventDto event) {
        educationRepository.deleteAllByResumeId(resume.getId());
        educationRepository.flush();

        if (event.getEducation() == null || event.getEducation().isEmpty()) return;

        List<ResumeEducation> educations = event.getEducation().stream()
                .map(edu -> ResumeEducation.builder()
                        .resume(resume)
                        .institution(edu.getSchool() != null ? edu.getSchool() : "Unknown")
                        .degree(edu.getDegree())
                        .major(edu.getMajor())
                        .build())
                .collect(Collectors.toList());

        educationRepository.saveAll(educations);
        log.debug("[MySQL Consumer] Đã lưu {} educations cho resumeId={}", educations.size(), resume.getId());
    }

    /** Gộp description + duration thành 1 chuỗi (duration từ LLM là text tự do, không có Date type) */
    private String buildExperienceDescription(CvParsedEventDto.ExperienceItemDto exp) {
        StringBuilder sb = new StringBuilder();
        if (exp.getDuration() != null && !exp.getDuration().isBlank()) {
            sb.append("[").append(exp.getDuration()).append("] ");
        }
        if (exp.getDescription() != null) {
            sb.append(exp.getDescription());
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
