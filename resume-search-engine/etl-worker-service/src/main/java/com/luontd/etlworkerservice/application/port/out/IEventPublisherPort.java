package com.luontd.etlworkerservice.application.port.out;

import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;

/**
 * Outbound port để publish CV parsed events tới downstream services qua message broker.
 *
 * <p>Application layer chỉ phụ thuộc vào interface này.
 * Adapter cụ thể (Kafka, RabbitMQ...) nằm ở infrastructure layer.
 */
public interface IEventPublisherPort {

    /**
     * Publish event để báo cáo CV đã được parse và index xong.
     *
     * @param event CV đã được parse bởi LLM
     */
    void publishCvParsedEvent(CvParsedEvent event);

    /**
     * Publish event để báo cáo lỗi trong quá trình ETL.
     *
     * @param resumeId ID của CV bị lỗi
     * @param reason Lý do lỗi
     */
    void publishCvEtlFailedEvent(java.util.UUID resumeId, String reason);
}
