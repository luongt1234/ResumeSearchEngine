package com.luontd.etlworkerservice.infrastructure.storage;

import com.luontd.etlworkerservice.application.dto.FileStorageDto;
import com.luontd.etlworkerservice.application.port.out.IFileStoragePort;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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

            if (!extension.equals(".pdf")){
                log.error("File không đúng định dạng: {}", originalFileName);
                throw new RuntimeException("Upload file thất bại!");
            }

            // Create bucket if not exists
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());

                log.info("Bucket created: {}", bucketName);
            }


            String newFileName = UUID.randomUUID().toString() + extension;

            String objectPath = String.format("batches/%s/%s", batchId, newFileName);
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectPath)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            log.info("Upload file lên MinIO thành công: {}", objectPath);

            return objectPath;
        } catch (Exception ex){
            log.error("Lỗi khi upload file lên MinIO", ex.getMessage());
            throw new RuntimeException("Upload file thất bại!");
        }
    }

    @Override
    public String DeleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                throw new RuntimeException("fileUrl không hợp lệ!");
            }

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileUrl)
                    .build()
            );

            log.info("Xóa file trên MinIO thành công: {}", fileUrl);
            return fileUrl;
        } catch (Exception ex) {
            log.error("Lỗi khi xóa file trên MinIO: {}", ex.getMessage());
            throw new RuntimeException("Xóa file thất bại!");
        }
    }

    @Override
    public FileStorageDto GetFileByFileUrl(String fileUrl) {
        try{
            if (fileUrl == null || fileUrl.isBlank()) {
                throw new RuntimeException("fileUrl không hợp lệ!");
            }

            String objectPath = fileUrl;

            try (InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            )) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int nRead;

                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();

                byte[] fileBytes = buffer.toByteArray();
                String fileName = objectPath.substring(objectPath.lastIndexOf("/") + 1);

                return FileStorageDto.builder()
                        .fileName(fileName)
                        .originalFileName(fileName)
                        .contentType("application/pdf")
                        .fileBytes(fileBytes)
                        .size(fileBytes.length)
                        .objectPath(objectPath)
                        .build();
            }
        } catch (Exception ex){
            log.error("Lỗi khi lấy file từ MinIO", ex.getMessage());
            throw new RuntimeException("Upload file thất bại!");
        }
    }
}
