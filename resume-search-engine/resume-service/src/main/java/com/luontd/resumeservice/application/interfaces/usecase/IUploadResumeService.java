package com.luontd.resumeservice.application.interfaces.usecase;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadResumeService {
    @Transactional
    String UploadResume(MultipartFile file, String batchIdStr, String userId);
}
