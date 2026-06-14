package com.luontd.resumeservice.infrastructure.messaging.kafka.producer;

import com.luontd.resumeservice.application.event.ResumeCreatedEvent;
import com.luontd.resumeservice.application.interfaces.outbound.IResumeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResumeProducer implements IResumeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(ResumeCreatedEvent event) {
        kafkaTemplate.send("resume-topic", event);
    }
}
