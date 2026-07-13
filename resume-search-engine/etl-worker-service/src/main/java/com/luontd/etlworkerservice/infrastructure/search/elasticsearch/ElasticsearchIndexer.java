package com.luontd.etlworkerservice.infrastructure.search.elasticsearch;

import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;
import com.luontd.etlworkerservice.application.dto.event.ExperienceDto;
import com.luontd.etlworkerservice.application.dto.event.EducationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kafka consumer lắng nghe topic cv-parsed-elasticsearch.
 * Nhiệm vụ: index CV data vào Elasticsearch để phục vụ keyword/full-text search.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchIndexer {

    private final CandidateSearchRepository candidateSearchRepository;

    public void index(CvParsedEvent event) {
        log.info("📥 [ETL→ES] Indexing event cv-parsed, resumeId={}", event.getResumeId());
        try {
            CandidateDocument doc = buildDocument(event);
            candidateSearchRepository.save(doc);
            log.info("✅ [ETL→ES] Index thành công vào Elasticsearch, resumeId={}", event.getResumeId());
        } catch (Exception e) {
            log.error("❌ [ETL→ES] Lỗi index vào Elasticsearch, resumeId={}: {}", event.getResumeId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi index Elasticsearch", e);
        }
    }

    // =========================================================================
    // Mapping helpers
    // =========================================================================

    private CandidateDocument buildDocument(CvParsedEvent event) {
        return CandidateDocument.builder()
                .resumeId(event.getResumeId().toString())
                .userId(event.getUserId() != null ? event.getUserId().toString() : null)
                .fullName(event.getFullName())
                .email(event.getEmail())
                .phone(event.getPhone())
                .skills(event.getSkills())
                .summary(event.getSummary())
                .experienceText(flattenExperience(event.getExperience()))
                .educationText(flattenEducation(event.getEducation()))
                .indexedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Gộp danh sách experience thành 1 chuỗi text để full-text search.
     * Ví dụ: "FPT Software | Senior Developer | 2021-2023 | Developed microservices..."
     */
    private String flattenExperience(List<ExperienceDto> experiences) {
        if (experiences == null || experiences.isEmpty()) return "";
        return experiences.stream()
                .map(e -> String.join(" | ",
                        nullToEmpty(e.getCompany()),
                        nullToEmpty(e.getTitle()),
                        nullToEmpty(e.getDuration()),
                        nullToEmpty(e.getDescription())
                ))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Gộp danh sách education thành 1 chuỗi text để full-text search.
     */
    private String flattenEducation(List<EducationDto> educations) {
        if (educations == null || educations.isEmpty()) return "";
        return educations.stream()
                .map(e -> String.join(" | ",
                        nullToEmpty(e.getSchool()),
                        nullToEmpty(e.getDegree()),
                        nullToEmpty(e.getMajor()),
                        nullToEmpty(e.getYear())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
