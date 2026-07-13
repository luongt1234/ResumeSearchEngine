package com.luontd.resumeservice.infrastructure.messaging.kafka.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mirror DTO của CvParsedEvent từ etl-worker-service.
 * Dùng để deserialize Kafka message từ topic cv-parsed-mysql.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvParsedEventDto {

    private UUID resumeId;
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private List<String> skills;
    private String summary;
    private List<ExperienceItemDto> experience;
    private List<EducationItemDto> education;
    private Map<String, Object> rawParsed;
    private LocalDateTime parsedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceItemDto {
        private String company;
        private String title;
        private String duration;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationItemDto {
        private String school;
        private String degree;
        private String major;
        private String year;
    }
}
