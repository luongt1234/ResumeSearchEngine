package com.luontd.etlworkerservice.infrastructure.message;

import com.luontd.etlworkerservice.application.dto.ResumeEventDto;
import com.luontd.etlworkerservice.application.port.in.IProcessCvUseCase;
import com.luontd.etlworkerservice.infrastructure.message.dto.ProcessCvJobEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @KafkaListener(
            topics = "${kafka.topic.cv-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeCvUploadedEvent(@Payload @Valid ProcessCvJobEvent event) {
        log.info("📥 [Kafka] Nhận job xử lý CV: fileId={}, userId={}", event.getFileId(), event.getUserId());
        try {
            // Chuyển message event thành ResumeEventDto cho application layer
            ResumeEventDto resumeEventDto = ResumeEventDto.builder()
                    .resumeId(UUID.fromString(event.getFileId()))
                    .userId(UUID.fromString(event.getUserId()))
                    .fileUrl(event.getFileId())   // fileId = key object trong MinIO/S3
                    .build();

            // Bàn giao cho application layer xử lý toàn bộ pipeline ETL
            _processCvUseCase.Execute(resumeEventDto);

            log.info("✅ [Kafka] Hoàn tất xử lý CV: fileId={}", event.getFileId());

        } catch (Exception e) {
            log.error("❌ [Kafka] Lỗi xử lý CV fileId={}: {}", event.getFileId(), e.getMessage(), e);

            // Dead Letter Queue — đẩy message lỗi sang DLQ để retry sau
            // _kafkaTemplate.send("process_cv_topic-dlq", event.getFileId(), event);
        }
    }
}
