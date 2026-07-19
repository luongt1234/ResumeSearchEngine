package com.luontd.etlworkerservice.application.port.out;

import com.luontd.etlworkerservice.application.dto.event.CvParsedEvent;

public interface IWeaviateIndexerPort {
    void index(CvParsedEvent event);
}
