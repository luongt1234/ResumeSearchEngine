package com.luontd.etlworkerservice.application.service;

import com.luontd.etlworkerservice.application.dto.ResumeEventDto;
import com.luontd.etlworkerservice.application.port.in.IProcessCvUseCase;
import com.luontd.etlworkerservice.application.port.out.IFileStoragePort;
import com.luontd.etlworkerservice.application.port.out.ILlmPort;
import com.luontd.etlworkerservice.application.port.out.IOcrPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCvUseCase implements IProcessCvUseCase {

    private final IFileStoragePort _fileStoragePort;
    private final IOcrPort _ocrPort;
    private final ILlmPort _llmPort;

    @Override
    public void Execute(ResumeEventDto event) {
        try {
            log.info("Bắt đầu xử lý CV, resumeId: {}", event.getResumeId());

            // Bước 1: Lấy file từ MinIO
            String fileUrl = event.getFileUrl();
            var fileDto = _fileStoragePort.GetFileByFileUrl(fileUrl);

            if (fileDto == null || fileDto.getFileBytes().length == 0) {
                throw new RuntimeException("File không tồn tại hoặc rỗng: " + fileUrl);
            }

            // Bước 2: OCR — trích xuất raw text từ file
            var fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String rawText = _ocrPort.extractText(fileDto.getFileBytes(), fileName);
            log.info("OCR hoàn thành, độ dài text: {} ký tự", rawText.length());

            // Bước 3: LLM — parse raw text thành Map dynamic theo cv-template.json
            // Khi đổi template, chỉ cần sửa cv-template.json, không cần sửa code
            Map<String, Object> parsedResume = _llmPort.parse(rawText);
            log.info("LLM parse hoàn thành, các trường trả về: {}", parsedResume.keySet());

            // TODO: Bước 4 — Persist parsedResume (Elasticsearch / Kafka / DB)

        } catch (Exception ex) {
            log.error("Lỗi khi xử lý CV, resumeId: {}", event.getResumeId(), ex);
            throw new RuntimeException("Xử lý CV thất bại!", ex);
        }
    }
}
