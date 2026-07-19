package com.luontd.etlworkerservice.application.service;

import com.luontd.etlworkerservice.application.dto.ResumeEventDto;
import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;
import com.luontd.etlworkerservice.application.dto.event.EducationDto;
import com.luontd.etlworkerservice.application.dto.event.ExperienceDto;
import com.luontd.etlworkerservice.application.port.in.IProcessCvUseCase;
import com.luontd.etlworkerservice.application.port.out.IEventPublisherPort;
import com.luontd.etlworkerservice.application.port.out.IFileStoragePort;
import com.luontd.etlworkerservice.application.port.out.ILlmPort;
import com.luontd.etlworkerservice.application.port.out.IOcrPort;
import com.luontd.etlworkerservice.application.port.out.IElasticsearchIndexerPort;
import com.luontd.etlworkerservice.application.port.out.IWeaviateIndexerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCvUseCase implements IProcessCvUseCase {

    private final IFileStoragePort _fileStoragePort;
    private final IOcrPort _ocrPort;
    private final ILlmPort _llmPort;
    private final IEventPublisherPort _eventPublisherPort;
    private final IElasticsearchIndexerPort _elasticsearchIndexer;
    private final IWeaviateIndexerPort _weaviateIndexer;

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

            // Bước 4: Map parsedResume (Map<String,Object>) → CvParsedEvent (typed DTO)
            CvParsedEvent cvParsedEvent = mapToCvParsedEvent(event, parsedResume);

            // Bước 5: Index dữ liệu vào Elasticsearch và Weaviate (Sync)
            _elasticsearchIndexer.index(cvParsedEvent);
            _weaviateIndexer.index(cvParsedEvent);

            // Bước 6: Publish Kafka message tới resume-service để lưu metadata
            _eventPublisherPort.publishCvParsedEvent(cvParsedEvent);

            log.info("✅ Đã xử lý và publish Kafka event thành công cho resumeId: {}", event.getResumeId());

        } catch (Exception ex) {
            log.error("Lỗi khi xử lý CV, resumeId: {}", event.getResumeId(), ex);
            try {
                _eventPublisherPort.publishCvEtlFailedEvent(event.getResumeId(), ex.getMessage());
            } catch (Exception publishEx) {
                log.error("Không thể publish event lỗi", publishEx);
            }
            throw new RuntimeException("Xử lý CV thất bại!", ex);
        }
    }

    // =========================================================================
    // Mapping helpers — chuyển Map<String,Object> từ LLM sang typed DTO
    // =========================================================================

    /**
     * Chuyển Map động từ LLM thành CvParsedEvent có kiểu tường minh.
     * Dùng null-safe helper để tránh ClassCastException khi LLM trả về format khác.
     */
    private CvParsedEvent mapToCvParsedEvent(ResumeEventDto event, Map<String, Object> parsed) {
        return CvParsedEvent.builder()
                .resumeId(event.getResumeId())
                .userId(event.getUserId())
                .fullName(getString(parsed, "fullName"))
                .email(getString(parsed, "email"))
                .phone(getString(parsed, "phone"))
                .skills(getStringList(parsed, "skills"))
                .summary(getString(parsed, "summary"))
                .experience(mapExperience(parsed))
                .education(mapEducation(parsed))
                .rawParsed(parsed)
                .parsedAt(LocalDateTime.now())
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<ExperienceDto> mapExperience(Map<String, Object> parsed) {
        Object raw = parsed.get("experience");
        if (!(raw instanceof List)) return Collections.emptyList();

        return ((List<?>) raw).stream()
                .filter(item -> item instanceof Map)
                .map(item -> {
                    Map<String, Object> m = (Map<String, Object>) item;
                    return ExperienceDto.builder()
                            .company(getString(m, "company"))
                            .title(getString(m, "title"))
                            .duration(getString(m, "duration"))
                            .description(getString(m, "description"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<EducationDto> mapEducation(Map<String, Object> parsed) {
        Object raw = parsed.get("education");
        if (!(raw instanceof List)) return Collections.emptyList();

        return ((List<?>) raw).stream()
                .filter(item -> item instanceof Map)
                .map(item -> {
                    Map<String, Object> m = (Map<String, Object>) item;
                    return EducationDto.builder()
                            .school(getString(m, "school"))
                            .degree(getString(m, "degree"))
                            .major(getString(m, "major"))
                            .year(getString(m, "year"))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /** Lấy String an toàn từ Map — trả null nếu key không tồn tại hoặc giá trị null */
    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    /** Lấy List<String> an toàn từ Map */
    @SuppressWarnings("unchecked")
    private List<String> getStringList(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (!(val instanceof List)) return Collections.emptyList();
        return ((List<?>) val).stream()
                .filter(item -> item != null)
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
