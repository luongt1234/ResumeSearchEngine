package com.luontd.etlworkerservice.infrastructure.message;

import com.luontd.etlworkerservice.application.dto.ResumeEventDto;
import com.luontd.etlworkerservice.application.port.in.IProcessCvUseCase;
import com.luontd.etlworkerservice.infrastructure.message.dto.ProcessCvJobEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaCvEventConsumer {

    private final IProcessCvUseCase _processCvUseCase;
    private final KafkaTemplate<String, Object> _kafkaTemplate;

    @Value("${kafka.topic.cv-uploaded-dlq:process_cv_topic-dlq}")
    private String dlqTopic;

    @KafkaListener(
            topics = "${kafka.topic.cv-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeCvUploadedEvent(@Payload @Valid ProcessCvJobEvent event) {
        log.info("📥 [Kafka] Nhận job xử lý CV: resumeId={}, userId={}", event.getResumeId(), event.getUserId());
        try {
            // Chuyển message event thành ResumeEventDto cho application layer
            ResumeEventDto resumeEventDto = ResumeEventDto.builder()
                    .resumeId(UUID.fromString(event.getResumeId()))
                    .userId(UUID.fromString(event.getUserId()))
                    .fileUrl(event.getFileUrl())   // fileUrl = key object trong MinIO/S3
                    .build();

            // Bàn giao cho application layer xử lý toàn bộ pipeline ETL
            _processCvUseCase.Execute(resumeEventDto);

            log.info("✅ [Kafka] Hoàn tất xử lý CV: resumeId={}", event.getResumeId());

        } catch (Exception e) {
            log.error("❌ [Kafka] Lỗi xử lý CV resumeId={}: {}", event.getResumeId(), e.getMessage(), e);

            // Dead Letter Queue — đẩy message lỗi sang DLQ để retry sau
            _kafkaTemplate.send(dlqTopic, event.getResumeId(), event).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error("❌ [Kafka] Gửi sang DLQ thất bại resumeId={}", event.getResumeId(), ex);
                } else {
                    log.info("✅ [Kafka] Đã chuyển message lỗi sang DLQ resumeId={}", event.getResumeId());
                }
            });
        }
    }
}
