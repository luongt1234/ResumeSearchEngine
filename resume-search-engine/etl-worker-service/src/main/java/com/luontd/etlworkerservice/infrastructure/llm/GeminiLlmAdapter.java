package com.luontd.etlworkerservice.infrastructure.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luontd.etlworkerservice.application.port.out.ILlmPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Adapter implements ILlmPort sử dụng Google Gemini Flash 1.5.
 *
 * <p>Trả về {@code Map<String, Object>} được deserialize trực tiếp từ JSON response
 * của Gemini — hoàn toàn dynamic theo cv-template.json, không bị ràng buộc bởi DTO cứng.
 *
 * <p>Để thay bằng provider khác, chỉ cần tạo class mới implements ILlmPort.
 */
@Slf4j
@Component
public class GeminiLlmAdapter implements ILlmPort {

    private static final String GEMINI_GENERATE_PATH =
            "/v1beta/models/{model}:generateContent?key={apiKey}";

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF =
            new TypeReference<>() {};

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final String templateJson;

    public GeminiLlmAdapter(
            @Qualifier("geminiRestClient") RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${app.llm.api-key}") String apiKey,
            @Value("${app.llm.model}") String model
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.templateJson = loadTemplate();
    }

    @Override
    public Map<String, Object> parse(String rawText) {
        try {
            String prompt = buildPrompt(rawText);
            String requestBody = buildRequestBody(prompt);

            log.info("Gửi request đến Gemini model: {}", model);

            String responseBody = restClient.post()
                    .uri(GEMINI_GENERATE_PATH, model, apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String jsonContent = extractJsonFromResponse(responseBody);

            // Deserialize thành Map<String, Object> — hoàn toàn dynamic theo template
            Map<String, Object> result = objectMapper.readValue(jsonContent, MAP_TYPE_REF);
            log.info("LLM parse thành công, các key trả về: {}", result.keySet());

            return result;
        } catch (Exception ex) {
            log.error("Lỗi khi gọi Gemini LLM: {}", ex.getMessage(), ex);
            throw new RuntimeException("Parse CV thất bại qua LLM!", ex);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    /**
     * Xây dựng prompt yêu cầu Gemini trả về JSON đúng template.
     * Template được đọc từ cv-template.json và nhúng trực tiếp vào prompt.
     */
    private String buildPrompt(String rawText) {
        return """
                Bạn là một hệ thống trích xuất thông tin CV chuyên nghiệp.
                Hãy đọc nội dung CV dưới đây và trả về JSON **chỉ JSON, không thêm bất kỳ văn bản nào khác**.
                Cấu trúc JSON phải khớp chính xác với template sau:
                
                %s
                
                Nội dung CV:
                ---
                %s
                ---
                
                Lưu ý:
                - Nếu không tìm thấy thông tin cho một trường, dùng null (với object/string) hoặc [] (với array).
                - Không bịa đặt thông tin không có trong CV.
                - Chỉ trả về JSON thuần túy, không bọc trong markdown code block.
                """.formatted(templateJson, rawText);
    }

    /**
     * Build JSON request body theo Gemini REST API format.
     */
    private String buildRequestBody(String prompt) {
        try {
            var partNode = objectMapper.createObjectNode();
            partNode.put("text", prompt);

            var partsArray = objectMapper.createArrayNode();
            partsArray.add(partNode);

            var content = objectMapper.createObjectNode();
            content.set("parts", partsArray);

            var contentsArray = objectMapper.createArrayNode();
            contentsArray.add(content);

            // Yêu cầu Gemini output JSON thuần
            var generationConfig = objectMapper.createObjectNode();
            generationConfig.put("response_mime_type", "application/json");

            var rootNode = objectMapper.createObjectNode();
            rootNode.set("contents", contentsArray);
            rootNode.set("generationConfig", generationConfig);

            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi build Gemini request body", ex);
        }
    }

    /**
     * Trích xuất chuỗi JSON từ response của Gemini API.
     * Response structure: candidates[0].content.parts[0].text
     */
    private String extractJsonFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text")
                .asText();

        if (text == null || text.isBlank()) {
            throw new RuntimeException("Gemini trả về nội dung rỗng");
        }

        return text.trim();
    }

    /**
     * Đọc cv-template.json từ classpath để nhúng vào prompt.
     * Template được load 1 lần khi khởi tạo bean.
     */
    private String loadTemplate() {
        try {
            var resource = getClass().getClassLoader().getResource("schemas/cv-template.json");
            if (resource == null) {
                throw new RuntimeException("Không tìm thấy schemas/cv-template.json trong classpath");
            }
            return Files.readString(Path.of(resource.toURI()), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Lỗi khi đọc cv-template.json", ex);
        }
    }
}
