package com.luontd.resumeservice.application.interfaces.usecase;

import com.luontd.resumeservice.presentation.dto.BatchSkillRequest;
import com.luontd.resumeservice.presentation.dto.BatchSkillResponse;

import java.util.List;
import java.util.UUID;

public interface IBatchSkillService {
    BatchSkillResponse addSkillToBatch(UUID batchId, BatchSkillRequest request);
    List<BatchSkillResponse> getSkillsByBatchId(UUID batchId);
    BatchSkillResponse updateBatchSkill(UUID batchId, UUID skillId, BatchSkillRequest request);
    void removeSkillFromBatch(UUID batchId, UUID skillId);
}
