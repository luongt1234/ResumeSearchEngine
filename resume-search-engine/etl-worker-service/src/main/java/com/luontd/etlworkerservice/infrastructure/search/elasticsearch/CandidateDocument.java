package com.luontd.etlworkerservice.infrastructure.search.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch document cho candidate — dùng cho keyword/full-text search.
 * Index: candidates (1 shard cho dev, tăng khi production).
 */
@Document(indexName = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDocument {

    @Id
    private String resumeId;   // UUID dạng String — ES dùng String làm ID

    @Field(type = FieldType.Keyword)
    private String userId;

    /** Full-text search: tên ứng viên */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullName;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String phone;

    /**
     * Skills — Keyword để filter exact + Text để full-text search.
     * Dùng multi-field mapping (cần cấu hình index mapping thủ công hoặc qua annotation).
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private List<String> skills;

    /** Tóm tắt kinh nghiệm — full-text search chính */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    /** Kinh nghiệm làm việc dạng text gộp — để full-text search */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String experienceText;

    /** Học vấn dạng text gộp — để full-text search */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String educationText;

    @Field(type = FieldType.Date)
    private LocalDateTime indexedAt;
}
