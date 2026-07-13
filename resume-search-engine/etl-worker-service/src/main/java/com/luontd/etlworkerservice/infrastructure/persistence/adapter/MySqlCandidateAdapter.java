package com.luontd.etlworkerservice.infrastructure.persistence.adapter;

import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Placeholder adapter cho MySQL trong ETL Worker.
 *
 * <p>Trong kiến trúc hiện tại, ETL Worker KHÔNG ghi thẳng vào MySQL —
 * thay vào đó publish Kafka event (cv-parsed-mysql) để resume-service xử lý.
 *
 * <p>File này được giữ lại để:
 *   1. Đáp ứng cấu trúc Hexagonal Architecture (Ports & Adapters)
 *   2. Có thể mở rộng nếu sau này cần ETL Worker ghi trực tiếp vào DB riêng
 *
 * <p>Nếu muốn ETL Worker có DB riêng (vd: lưu ETL job logs, audit trail),
 * hãy inject DataSource + JPA vào đây.
 */
@Component
@Slf4j
public class MySqlCandidateAdapter {

    /**
     * Log thông tin CV đã parse (dùng để audit/debug).
     * Trong production, có thể ghi vào bảng etl_job_logs.
     *
     * @param event CV parsed event
     */
    public void logParsedEvent(CvParsedEvent event) {
        log.info("[MySqlCandidateAdapter] CV parsed — resumeId={}, fullName={}, skills={}",
                event.getResumeId(),
                event.getFullName(),
                event.getSkills() != null ? event.getSkills().size() + " skills" : "none"
        );
    }
}
