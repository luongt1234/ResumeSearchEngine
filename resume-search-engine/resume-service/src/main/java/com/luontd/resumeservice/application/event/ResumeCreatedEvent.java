package com.luontd.resumeservice.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeCreatedEvent {
    private UUID resumeId;
    private UUID userId;
    private String fileUrl;
}