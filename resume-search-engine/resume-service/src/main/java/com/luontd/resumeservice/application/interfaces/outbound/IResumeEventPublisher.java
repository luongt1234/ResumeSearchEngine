package com.luontd.resumeservice.application.interfaces.outbound;

import com.luontd.resumeservice.application.event.ResumeCreatedEvent;

public interface IResumeEventPublisher {
    void publish(ResumeCreatedEvent event);
}
