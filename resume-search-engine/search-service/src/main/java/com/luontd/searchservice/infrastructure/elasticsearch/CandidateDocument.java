package com.luontd.searchservice.infrastructure.elasticsearch;

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

@Document(indexName = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDocument {

    @Id
    private String resumeId;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String fullName;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Text, analyzer = "standard")
    private List<String> skills;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String summary;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String experienceText;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String educationText;

    @Field(type = FieldType.Date)
    private LocalDateTime indexedAt;
}
