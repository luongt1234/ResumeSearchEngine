package com.luontd.etlworkerservice.application.port.out;

import java.util.Map;

/**
 * Outbound port cho LLM.
 *
 * <p>Bất kỳ LLM provider nào (Gemini, OpenAI, Claude...) đều phải implement interface này.
 * ProcessCvUseCase chỉ phụ thuộc vào interface này, không biết đến provider cụ thể.
 *
 * <p>Trả về {@code Map<String, Object>} thay vì typed DTO để hoàn toàn dynamic theo
 * cv-template.json — khi đổi template chỉ cần sửa file JSON, không cần sửa Java.
 */
public interface ILlmPort {

    /**
     * Parse raw text từ OCR thành structured data theo cv-template.json.
     *
     * @param rawText Văn bản thô trích xuất từ OCR
     * @return Map dynamic khớp với cấu trúc cv-template.json
     */
    Map<String, Object> parse(String rawText);
}
