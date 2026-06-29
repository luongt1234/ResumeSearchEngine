package com.luontd.etlworkerservice.infrastructure.message;

import com.luontd.etlworkerservice.infrastructure.message.dto.ProcessCvJobEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaCvEventConsumer {

    @KafkaListener(
            topics = "${kafka.topic.cv-uploaded}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeCvUploadedEvent(@Payload @Valid ProcessCvJobEvent event) {
        try {
            // Chuyển giao fileId cho tầng Application (IProcessCvUseCase) xử lý
            // processCvUseCase.execute(event.getFileId());

            log.info("✅ [Kafka] Hoàn tất xử lý CV: fileId={}", event.getFileId());

        } catch (Exception e) {
            log.error("❌ [Kafka] Lỗi xử lý CV fileId={}: {}", event.getFileId(), e.getMessage());

            // Xử lý lỗi Cứng: Đẩy message này vào sọt rác DLQ (Dead Letter Queue) trong Kafka
            // kafkaTemplate.send("cv-uploaded-dlq", event);
        }
    }
}
