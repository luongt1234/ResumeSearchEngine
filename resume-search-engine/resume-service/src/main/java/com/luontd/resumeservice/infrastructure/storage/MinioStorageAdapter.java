package com.luontd.resumeservice.infrastructure.storage;

import com.luontd.resumeservice.application.interfaces.outbound.IFileStoragePort;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements IFileStoragePort {
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public String UploadFile(MultipartFile file, String batchId) {
        try{
            String originalFileName = file.getOriginalFilename();
            String extension = originalFileName != null
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : ".pdf";

            if (extension.equals(".pdf")){
                log.error("File không đúng định dạng: {}", originalFileName);
                throw new RuntimeException("Upload file thất bại!");
            }

            String newFileName = UUID.randomUUID().toString() + extension;

            String objectPath = String.format("batches/%s/%s", batchId, newFileName);
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(newFileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            log.info("Upload file lên MinIO thành công: {}", newFileName);

            return newFileName;
        } catch (Exception ex){
            log.error("Lỗi khi upload file lên MinIO", ex.getMessage());
            throw new RuntimeException("Upload file thất bại!");
        }
    }

    @Override
    public String DeleteFile(String fileUrl) {
        return "";
    }

    @Override
    public MultipartFile GetFileByFileUrl(String fileUrl) {
        return null;
    }
}
