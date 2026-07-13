package com.luontd.etlworkerservice.infrastructure.search.weaviate;

import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;
import com.luontd.etlworkerservice.application.dto.event.ExperienceDto;
import com.luontd.etlworkerservice.application.dto.event.EducationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Kafka consumer lắng nghe topic cv-parsed-embedding.
 * Flow:
 *  1. Nhận CvParsedEvent từ Kafka
 *  2. Gọi Embedding API (OpenAI-compatible) để lấy vector của CV text
 *  3. Upsert object vào Weaviate (vector database) cho semantic search
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WeaviateIndexer {

    private final WebClient.Builder webClientBuilder;

    @Value("${weaviate.url}")
    private String weaviateUrl;

    @Value("${weaviate.class-name:Candidate}")
    private String weaviateClassName;

    @Value("${weaviate.skill-class-name:CandidateSkill}")
    private String skillClassName;

    @Value("${embedding.api.url}")
    private String embeddingApiUrl;

    @Value("${embedding.api.key:}")
    private String embeddingApiKey;

    @Value("${embedding.model:text-embedding-ada-002}")
    private String embeddingModel;

    public void index(CvParsedEvent event) {
        log.info("📥 [Kafka→Weaviate] Nhận event cv-parsed, resumeId={}", event.getResumeId());
        try {
            // Bước 1: Tạo text đại diện cho CV (dùng để embed)
            String cvText = buildCvText(event);

            // Bước 2: Gọi Embedding API lấy vector
            List<Double> vector = getEmbedding(cvText);

            // Bước 3: Upsert Candidate vào Weaviate
            upsertToWeaviate(event, vector);
            log.info("✅ [Kafka→Weaviate] Upsert Candidate thành công, resumeId={}", event.getResumeId());

            // Bước 4: Upsert CandidateSkill (từng kỹ năng)
            if (event.getSkills() != null && !event.getSkills().isEmpty()) {
                for (String skill : event.getSkills()) {
                    if (skill == null || skill.isBlank()) continue;
                    List<Double> skillVector = getEmbedding(skill.trim());
                    upsertSkillToWeaviate(event.getResumeId().toString(), skill.trim(), skillVector);
                }
                log.info("✅ [Kafka→Weaviate] Upsert {} skills thành công, resumeId={}", event.getSkills().size(), event.getResumeId());
            }
        } catch (Exception e) {
            log.error("❌ [Kafka→Weaviate] Lỗi upsert Weaviate, resumeId={}: {}", event.getResumeId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi upsert Weaviate", e);
        }
    }

    // =========================================================================
    // Embedding API (OpenAI-compatible format)
    // =========================================================================

    /**
     * Gọi Embedding API để lấy vector của text.
     * Hỗ trợ bất kỳ API nào theo chuẩn OpenAI (OpenAI, Azure OpenAI, local model...).
     */
    @SuppressWarnings("unchecked")
    private List<Double> getEmbedding(String text) {
        WebClient client = webClientBuilder.baseUrl(embeddingApiUrl).build();

        Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", embeddingModel
        );

        Map<String, Object> response = client.post()
                .uri("/v1/embeddings")
                .header("Authorization", "Bearer " + embeddingApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) throw new RuntimeException("Embedding API trả về response rỗng");

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null || data.isEmpty()) throw new RuntimeException("Embedding API không trả về data");

        return (List<Double>) data.get(0).get("embedding");
    }

    // =========================================================================
    // Weaviate REST API
    // =========================================================================

    /**
     * Upsert candidate object vào Weaviate.
     * Dùng PUT /v1/objects/{className}/{uuid} để đảm bảo idempotent.
     */
    private void upsertToWeaviate(CvParsedEvent event, List<Double> vector) {
        WebClient client = webClientBuilder.baseUrl(weaviateUrl).build();

        // Properties được lưu trong Weaviate (metadata kèm theo vector)
        Map<String, Object> properties = new HashMap<>();
        properties.put("resumeId", event.getResumeId().toString());
        properties.put("userId", event.getUserId() != null ? event.getUserId().toString() : "");
        properties.put("fullName", nullToEmpty(event.getFullName()));
        properties.put("skills", event.getSkills() != null ? String.join(", ", event.getSkills()) : "");
        properties.put("summary", nullToEmpty(event.getSummary()));

        // Weaviate object với vector đính kèm
        Map<String, Object> weaviateObject = new HashMap<>();
        weaviateObject.put("class", weaviateClassName);
        weaviateObject.put("id", event.getResumeId().toString());  // Dùng resumeId làm Weaviate UUID
        weaviateObject.put("properties", properties);
        weaviateObject.put("vector", vector);

        // PUT để upsert (tạo mới hoặc ghi đè nếu đã tồn tại)
        client.put()
                .uri("/v1/objects/{className}/{id}", weaviateClassName, event.getResumeId().toString())
                .header("Content-Type", "application/json")
                .bodyValue(weaviateObject)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    /**
     * Upsert CandidateSkill object vào Weaviate.
     * Sử dụng resumeId và skillName để tạo một UUID duy nhất cho record này.
     */
    private void upsertSkillToWeaviate(String resumeId, String skillName, List<Double> vector) {
        WebClient client = webClientBuilder.baseUrl(weaviateUrl).build();

        // Tạo UUID từ resumeId và skillName để đảm bảo idempotent
        String idStr = resumeId + "-" + skillName.toLowerCase();
        String uuid = UUID.nameUUIDFromBytes(idStr.getBytes()).toString();

        Map<String, Object> properties = new HashMap<>();
        properties.put("resumeId", resumeId);
        properties.put("skillName", skillName);

        Map<String, Object> weaviateObject = new HashMap<>();
        weaviateObject.put("class", skillClassName);
        weaviateObject.put("id", uuid);
        weaviateObject.put("properties", properties);
        weaviateObject.put("vector", vector);

        client.put()
                .uri("/v1/objects/{className}/{id}", skillClassName, uuid)
                .header("Content-Type", "application/json")
                .bodyValue(weaviateObject)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Tạo chuỗi text đại diện cho toàn bộ CV — dùng để embed.
     * Bao gồm: skills, summary, experience, education.
     */
    private String buildCvText(CvParsedEvent event) {
        StringBuilder sb = new StringBuilder();

        if (event.getFullName() != null) sb.append("Name: ").append(event.getFullName()).append("\n");

        if (event.getSkills() != null && !event.getSkills().isEmpty()) {
            sb.append("Skills: ").append(String.join(", ", event.getSkills())).append("\n");
        }

        if (event.getSummary() != null) sb.append("Summary: ").append(event.getSummary()).append("\n");

        if (event.getExperience() != null) {
            sb.append("Experience:\n");
            event.getExperience().forEach(exp ->
                    sb.append("- ").append(nullToEmpty(exp.getTitle()))
                      .append(" at ").append(nullToEmpty(exp.getCompany()))
                      .append(" (").append(nullToEmpty(exp.getDuration())).append("): ")
                      .append(nullToEmpty(exp.getDescription())).append("\n")
            );
        }

        if (event.getEducation() != null) {
            sb.append("Education:\n");
            event.getEducation().forEach(edu ->
                    sb.append("- ").append(nullToEmpty(edu.getDegree()))
                      .append(" in ").append(nullToEmpty(edu.getMajor()))
                      .append(" at ").append(nullToEmpty(edu.getSchool()))
                      .append(" (").append(nullToEmpty(edu.getYear())).append(")\n")
            );
        }

        return sb.toString();
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
