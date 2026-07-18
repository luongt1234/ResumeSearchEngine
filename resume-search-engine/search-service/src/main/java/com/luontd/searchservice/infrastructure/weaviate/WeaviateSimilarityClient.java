package com.luontd.searchservice.infrastructure.weaviate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import net.devh.boot.grpc.client.inject.GrpcClient;
import com.luontd.grpc.embedding.EmbeddingServiceGrpc;
import com.luontd.grpc.embedding.EmbeddingProto.EmbeddingRequest;
import com.luontd.grpc.embedding.EmbeddingProto.EmbeddingResponse;
import com.luontd.grpc.embedding.EmbeddingProto.EmbeddingBatchRequest;
import com.luontd.grpc.embedding.EmbeddingProto.EmbeddingBatchResponse;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client giao tiếp với Weaviate và Embedding API phục vụ chấm điểm CV.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WeaviateSimilarityClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${weaviate.url}")
    private String weaviateUrl;

    @Value("${weaviate.class-name:Candidate}")
    private String candidateClassName;

    @Value("${weaviate.skill-class-name:CandidateSkill}")
    private String skillClassName;

    @GrpcClient("embedding-service")
    private EmbeddingServiceGrpc.EmbeddingServiceBlockingStub embeddingServiceStub;

    /**
     * Gọi Embedding API lấy vector của text.
     */
    /**
     * Gọi gRPC Embedding API lấy vector của text.
     */
    public List<Double> getEmbedding(String text) {
        EmbeddingRequest request = EmbeddingRequest.newBuilder()
                .setText(text)
                .build();
        
        EmbeddingResponse response = embeddingServiceStub.generateEmbedding(request);
        
        List<Double> result = new ArrayList<>();
        for (float val : response.getEmbeddingList()) {
            result.add((double) val);
        }
        return result;
    }

    /**
     * Truy vấn mức độ tương đồng của JD với danh sách Resume (Candidate class).
     * Trả về Map<resumeId, certainty>
     */
    @SuppressWarnings("unchecked")
    public Map<String, Double> getSemanticScores(List<Double> jdVector, List<String> resumeIds) {
        WebClient client = webClientBuilder.baseUrl(weaviateUrl).build();

        String idsArray = resumeIds.stream()
                .map(id -> "\"" + id + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        String query = String.format("""
            {
              Get {
                %s(
                  nearVector: { vector: %s }
                  where: { path: ["resumeId"], operator: ContainsAny, valueText: [%s] }
                ) {
                  resumeId
                  _additional { certainty }
                }
              }
            }
            """, candidateClassName, jdVector.toString(), idsArray);

        Map<String, Object> requestBody = Map.of("query", query);

        Map<String, Object> response = client.post()
                .uri("/v1/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Double> result = new HashMap<>();

        if (response != null && response.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> get = (Map<String, Object>) data.get("Get");
            if (get != null && get.containsKey(candidateClassName)) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) get.get(candidateClassName);
                for (Map<String, Object> cand : candidates) {
                    String resumeId = (String) cand.get("resumeId");
                    Map<String, Object> additional = (Map<String, Object>) cand.get("_additional");
                    Double certainty = ((Number) additional.get("certainty")).doubleValue();
                    result.put(resumeId, certainty);
                }
            }
        }
        return result;
    }

    /**
     * Tìm kiếm top K candidates bằng Semantic Search (Vector).
     * Trả về Map<resumeId, certainty>.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Double> searchCandidates(List<Double> queryVector, int limit) {
        WebClient client = webClientBuilder.baseUrl(weaviateUrl).build();

        String query = String.format("""
            {
              Get {
                %s(
                  nearVector: { vector: %s }
                  limit: %d
                ) {
                  resumeId
                  _additional { certainty }
                }
              }
            }
            """, candidateClassName, queryVector.toString(), limit);

        Map<String, Object> requestBody = Map.of("query", query);

        Map<String, Object> response = client.post()
                .uri("/v1/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, Double> result = new HashMap<>();

        if (response != null && response.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> get = (Map<String, Object>) data.get("Get");
            if (get != null && get.containsKey(candidateClassName)) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) get.get(candidateClassName);
                for (Map<String, Object> cand : candidates) {
                    String resumeId = (String) cand.get("resumeId");
                    Map<String, Object> additional = (Map<String, Object>) cand.get("_additional");
                    Double certainty = ((Number) additional.get("certainty")).doubleValue();
                    result.put(resumeId, certainty);
                }
            }
        }
        return result;
    }

    /**
     * Truy vấn độ tương đồng của một Kỹ năng yêu cầu (BatchSkill) với danh sách các kỹ năng của CV (CandidateSkill class).
     * Tìm kỹ năng gần nhất cho mỗi Resume.
     * Trả về Map<resumeId, SkillMatchResult>
     */
    @SuppressWarnings("unchecked")
    public Map<String, SkillMatchResult> getBestSkillMatches(List<Double> batchSkillVector, List<String> resumeIds) {
        WebClient client = webClientBuilder.baseUrl(weaviateUrl).build();

        String idsArray = resumeIds.stream()
                .map(id -> "\"" + id + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("");

        String query = String.format("""
            {
              Get {
                %s(
                  nearVector: { vector: %s }
                  where: { path: ["resumeId"], operator: ContainsAny, valueText: [%s] }
                ) {
                  resumeId
                  skillName
                  _additional { certainty }
                }
              }
            }
            """, skillClassName, batchSkillVector.toString(), idsArray);

        Map<String, Object> requestBody = Map.of("query", query);

        Map<String, Object> response = client.post()
                .uri("/v1/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        Map<String, SkillMatchResult> bestMatches = new HashMap<>();

        if (response != null && response.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> get = (Map<String, Object>) data.get("Get");
            if (get != null && get.containsKey(skillClassName)) {
                List<Map<String, Object>> skills = (List<Map<String, Object>>) get.get(skillClassName);
                for (Map<String, Object> skillMap : skills) {
                    String resumeId = (String) skillMap.get("resumeId");
                    String matchedSkillName = (String) skillMap.get("skillName");
                    Map<String, Object> additional = (Map<String, Object>) skillMap.get("_additional");
                    Double certainty = ((Number) additional.get("certainty")).doubleValue();

                    if (!bestMatches.containsKey(resumeId) || bestMatches.get(resumeId).certainty() < certainty) {
                        bestMatches.put(resumeId, new SkillMatchResult(matchedSkillName, certainty));
                    }
                }
            }
        }
        return bestMatches;
    }

    public record SkillMatchResult(String matchedSkillName, Double certainty) {}
}
