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

    // Chốt chặn Validate: Không được để trống
    @NotBlank(message = "fileId không hợp lệ hoặc bị trống")
    private String fileId;

    @NotBlank(message = "userId không hợp lệ hoặc bị trống")
    private String userId;
}