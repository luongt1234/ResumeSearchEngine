package com.luontd.resumeservice.infrastructure.messaging.kafka.consumer;

import com.luontd.resumeservice.application.services.SaveParsedResumeService;
import com.luontd.resumeservice.infrastructure.messaging.kafka.consumer.dto.CvParsedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer lắng nghe topic cv-parsed-mysql.
 * Được publish bởi etl-worker-service sau khi LLM parse xong CV.
 * Nhiệm vụ: lưu metadata candidate vào MySQL (resume-service DB).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CvParsedMysqlConsumer {

    private final SaveParsedResumeService saveParsedResumeService;

    @KafkaListener(
            topics = "${kafka.topic.cv-parsed-mysql}",
            groupId = "${kafka.consumer.group.cv-parsed}",
            containerFactory = "cvParsedEventListenerContainerFactory"
    )
    public void consume(@Payload CvParsedEventDto event) {
        log.info("📥 [Kafka→MySQL] Nhận event cv-parsed, resumeId={}", event.getResumeId());
        try {
            saveParsedResumeService.save(event);
        } catch (IllegalStateException e) {
            // Resume không tồn tại — bỏ qua (có thể đã bị xóa)
            log.warn("⚠️ [Kafka→MySQL] Resume không tồn tại, bỏ qua — resumeId={}: {}", event.getResumeId(), e.getMessage());
        } catch (Exception e) {
            log.error("❌ [Kafka→MySQL] Lỗi lưu CV vào MySQL, resumeId={}: {}", event.getResumeId(), e.getMessage(), e);
            
            // Đổi trạng thái trong DB thành FAILED
            saveParsedResumeService.updateStatusFailed(event.getResumeId(), e.getMessage());
            
            // Re-throw để Spring Kafka retry / đẩy vào DLQ nếu được cấu hình
            throw new RuntimeException("Lỗi lưu CV vào MySQL", e);
        }
    }

    @KafkaListener(
            topics = "${kafka.topic.cv-etl-failed:cv-etl-failed}",
            groupId = "${kafka.consumer.group.cv-parsed}"
    )
    public void consumeFailed(java.util.Map<String, String> event) {
        String resumeId = event.get("resumeId");
        String reason = event.get("reason");
        log.info("📥 [Kafka→MySQL] Nhận event cv-etl-failed, resumeId={}", resumeId);
        try {
            saveParsedResumeService.updateStatusFailed(java.util.UUID.fromString(resumeId), reason);
        } catch (Exception e) {
            log.error("❌ [Kafka→MySQL] Lỗi xử lý CV failed, resumeId={}: {}", resumeId, e.getMessage(), e);
        }
    }
}
