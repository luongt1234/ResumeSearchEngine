package com.luontd.resumeservice.application.services;

import com.luontd.resumeservice.application.interfaces.outbound.IFileStoragePort;
import com.luontd.resumeservice.application.interfaces.outbound.IResumeEventPublisher;
import com.luontd.resumeservice.application.interfaces.repository.IResumeBatchRepository;
import com.luontd.resumeservice.application.interfaces.repository.IResumeRepository;
import com.luontd.resumeservice.application.interfaces.usecase.IUploadResumeService;
import com.luontd.resumeservice.domain.entity.Resume;
import com.luontd.resumeservice.domain.enums.EtlStatus;
import com.luontd.resumeservice.application.event.ResumeCreatedEvent;
import com.luontd.resumeservice.utils.UuidUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadResumeService implements IUploadResumeService {
    private final IFileStoragePort _fileStorage;
    private final IResumeBatchRepository _resumeBatch;
    private final IResumeEventPublisher _resumeProducer;
    private final IResumeRepository _resumeRepository;

    @Transactional
    @Override
    public String UploadResume(MultipartFile file, String batchIdStr, String userId) {
        String fileName = file.getOriginalFilename();

        if (!org.springframework.util.StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Upload resume error: Tên file không hợp lệ!");
        }
        // upload file to minio
        String urlResume = _fileStorage.UploadFile(file, batchIdStr);

        if (!StringUtils.hasText(urlResume)){
            throw new RuntimeException("Upload resume error: urlResume is empty");
        }

        // find batch by batch id
        UUID batchId = UuidUtils.tryParse(batchIdStr)
                .orElseThrow(() -> new IllegalArgumentException("Upload resume error: Batch ID không hợp lệ, vui lòng kiểm tra lại!"));

        UUID userIdUUID =  UuidUtils.tryParse(userId)
                .orElseThrow(() -> new IllegalArgumentException("Upload resume error: User ID không hợp lệ, vui lòng kiểm tra lại!"));

        var batch = _resumeBatch.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Upload resume error: Batch not found"));

        // create resume in database
        var resume = Resume.builder()
                .userId(userId)
                .fileName(fileName)
                .fileUrl(urlResume)
                .etlStatus(EtlStatus.PENDING)
                .batch(batch)
                .build();

        var savedResume = _resumeRepository.save(resume);
        // send message to kafka
        UUID resumeId = savedResume.getId();

        var event = ResumeCreatedEvent.builder()
                .userId(userIdUUID)
                .fileUrl(urlResume)
                .resumeId(resumeId)
                .build();
        _resumeProducer.publish(event);

        return urlResume;
    }
}
