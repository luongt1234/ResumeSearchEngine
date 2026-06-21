package com.luontd.etlworkerservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStorageDto {
    private String fileName;
    private String originalFileName;
    private String contentType;
    private byte[] fileBytes;
    private long size;
    private String objectPath;
}
