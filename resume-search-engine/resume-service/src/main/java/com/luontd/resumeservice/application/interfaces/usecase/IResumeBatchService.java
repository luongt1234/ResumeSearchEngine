package com.luontd.resumeservice.application.interfaces.usecase;

import com.luontd.resumeservice.presentation.dto.ResumeBatchRequest;
import com.luontd.resumeservice.presentation.dto.ResumeBatchResponse;

import java.util.List;
import java.util.UUID;

public interface IResumeBatchService {
    ResumeBatchResponse createBatch(ResumeBatchRequest request);
    ResumeBatchResponse getBatchById(UUID id);
    List<ResumeBatchResponse> getAllBatches();
    ResumeBatchResponse updateBatch(UUID id, ResumeBatchRequest request);
    void deleteBatch(UUID id);
}
