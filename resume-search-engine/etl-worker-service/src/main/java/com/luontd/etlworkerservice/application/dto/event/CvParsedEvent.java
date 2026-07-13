package com.luontd.etlworkerservice.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Event DTO được publish lên Kafka sau khi LLM parse xong CV.
 * Được gửi tới 3 topics: cv-parsed-mysql, cv-parsed-elasticsearch, cv-parsed-embedding.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvParsedEvent {

    /** ID của bản ghi CV trong hệ thống */
    private UUID resumeId;

    /** ID người dùng upload CV */
    private UUID userId;

    /** Họ tên ứng viên */
    private String fullName;

    /** Email ứng viên */
    private String email;

    /** Số điện thoại ứng viên */
    private String phone;

    /** Danh sách kỹ năng (dùng cho keyword search và embedding) */
    private List<String> skills;

    /**
     * Tóm tắt kinh nghiệm dạng raw text.
     * Dùng cho full-text search (Elasticsearch) và embedding (Weaviate).
     */
    private String summary;

    /** Danh sách kinh nghiệm làm việc */
    private List<ExperienceDto> experience;

    /** Danh sách học vấn */
    private List<EducationDto> education;

    /**
     * Toàn bộ Map raw từ LLM — giữ lại để không mất data khi cv-template.json thay đổi.
     * MySQL và Elasticsearch có thể lưu dưới dạng JSON column / dynamic mapping.
     */
    private Map<String, Object> rawParsed;

    /** Thời điểm LLM parse xong */
    private LocalDateTime parsedAt;
}
