package com.luontd.etlworkerservice.infrastructure.messaging.publisher;

import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;
import com.luontd.etlworkerservice.application.port.out.IEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter implements IEventPublisherPort.
 * Publish CV parsed events tới 3 downstream topics sau khi LLM trả về Structured JSON.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements IEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.cv-parsed-events}")
    private String cvParsedEventsTopic;

    @Value("${kafka.topic.cv-etl-failed:cv-etl-failed}")
    private String cvEtlFailedTopic;

    @Override
    public void publishCvParsedEvent(CvParsedEvent event) {
        publish(cvParsedEventsTopic, event, "cv-parsed-events");
    }

    @Override
    public void publishCvEtlFailedEvent(java.util.UUID resumeId, String reason) {
        publish(cvEtlFailedTopic, java.util.Map.of("resumeId", resumeId, "reason", reason), "cv-etl-failed");
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Gửi message tới Kafka topic và gắn callback để log kết quả async.
     *
     * @param topic     Tên Kafka topic
     * @param event     Payload event
     * @param target    Tên service đích (chỉ dùng để log)
     */
    private void publish(String topic, Object event, String target) {
        String key = "";
        if (event instanceof CvParsedEvent) {
            key = ((CvParsedEvent) event).getResumeId().toString();
        } else if (event instanceof java.util.Map) {
            key = ((java.util.Map<?, ?>) event).get("resumeId").toString();
        }
        
        final String messageKey = key;

        CompletableFuture<?>  future = kafkaTemplate.send(topic, messageKey, event).toCompletableFuture();

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ [Kafka] Gửi event tới {} thất bại — resumeId={}, topic={}, error={}",
                        target, messageKey, topic, ex.getMessage(), ex);
            } else {
                log.info("✅ [Kafka] Gửi event tới {} thành công — resumeId={}, topic={}",
                        target, messageKey, topic);
            }
        });
    }
}
