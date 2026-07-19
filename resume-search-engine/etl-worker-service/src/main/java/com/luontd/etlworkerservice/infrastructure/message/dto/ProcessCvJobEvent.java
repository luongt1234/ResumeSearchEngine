package com.luontd.etlworkerservice.infrastructure.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCvJobEvent {

    @NotBlank(message = "resumeId không hợp lệ hoặc bị trống")
    private String resumeId;

    @NotBlank(message = "userId không hợp lệ hoặc bị trống")
    private String userId;

    @NotBlank(message = "fileUrl không hợp lệ hoặc bị trống")
    private String fileUrl;
}