package com.luontd.resumeservice.application.interfaces.outbound;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStoragePort {
    String UploadFile(MultipartFile file, String batchId);
    String DeleteFile(String fileUrl);
    MultipartFile GetFileByFileUrl(String fileUrl);
}
