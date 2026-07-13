package com.luontd.etlworkerservice.infrastructure.search.weaviate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Khởi tạo Weaviate schema khi ứng dụng start.
 * Tạo class "Candidate" nếu chưa tồn tại.
 * Implements ApplicationRunner để chạy sau khi Spring context load xong.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WeaviateSchemaInitializer implements ApplicationRunner {

    private final WebClient.Builder webClientBuilder;

    @Value("${weaviate.url}")
    private String weaviateUrl;

    @Value("${weaviate.class-name:Candidate}")
    private String className;

    @Value("${weaviate.skill-class-name:CandidateSkill}")
    private String skillClassName;

    @Override
    public void run(ApplicationArguments args) {
        try {
            WebClient client = webClientBuilder.baseUrl(weaviateUrl).build();

            // Kiểm tra và tạo class Candidate
            if (!classExists(client, className)) {
                createClass(client, className, "CV candidate data for semantic search", List.of(
                        property("resumeId", "text", "UUID của CV trong hệ thống"),
                        property("userId", "text", "UUID của người dùng"),
                        property("fullName", "text", "Họ tên ứng viên"),
                        property("skills", "text", "Danh sách kỹ năng (comma-separated)"),
                        property("summary", "text", "Tóm tắt kinh nghiệm")
                ));
                log.info("[Weaviate] ✅ Khởi tạo class '{}' thành công.", className);
            } else {
                log.info("[Weaviate] Class '{}' đã tồn tại, bỏ qua khởi tạo.", className);
            }

            // Kiểm tra và tạo class CandidateSkill
            if (!classExists(client, skillClassName)) {
                createClass(client, skillClassName, "Individual skill vector for candidate semantic skill matching", List.of(
                        property("resumeId", "text", "UUID của CV"),
                        property("skillName", "text", "Tên kỹ năng")
                ));
                log.info("[Weaviate] ✅ Khởi tạo class '{}' thành công.", skillClassName);
            } else {
                log.info("[Weaviate] Class '{}' đã tồn tại, bỏ qua khởi tạo.", skillClassName);
            }

        } catch (Exception e) {
            // Không throw để app không bị crash nếu Weaviate chưa sẵn sàng
            log.warn("[Weaviate] ⚠️ Không thể khởi tạo schema (Weaviate có thể chưa chạy): {}", e.getMessage());
        }
    }

    private boolean classExists(WebClient client, String checkClassName) {
        try {
            client.get()
                    .uri("/v1/schema/{className}", checkClassName)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        }
    }

    /**
     * Tạo Weaviate class schema với các properties cần thiết.
     * vectorizer: none — vì vector được cung cấp thủ công từ Embedding API.
     */
    private void createClass(WebClient client, String targetClassName, String description, List<Map<String, Object>> properties) {
        Map<String, Object> schema = Map.of(
                "class", targetClassName,
                "description", description,
                "vectorizer", "none",   // Vector được inject thủ công
                "properties", properties
        );

        client.post()
                .uri("/v1/schema")
                .header("Content-Type", "application/json")
                .bodyValue(schema)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private Map<String, Object> property(String name, String dataType, String description) {
        return Map.of(
                "name", name,
                "dataType", List.of(dataType),
                "description", description
        );
    }
}
