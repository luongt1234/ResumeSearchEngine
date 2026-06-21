package com.luontd.etlworkerservice.application.port.out;

import com.luontd.etlworkerservice.application.dto.FileStorageDto;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStoragePort {
    String UploadFile(MultipartFile file, String batchId);
    String DeleteFile(String fileUrl);
    FileStorageDto GetFileByFileUrl(String fileUrl);
}
