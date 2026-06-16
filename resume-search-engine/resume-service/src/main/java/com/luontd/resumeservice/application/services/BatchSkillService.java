package com.luontd.resumeservice.application.services;

import com.luontd.resumeservice.application.interfaces.usecase.IBatchSkillService;
import com.luontd.resumeservice.application.interfaces.repository.IBatchSkillRepository;
import com.luontd.resumeservice.application.interfaces.repository.IResumeBatchRepository;
import com.luontd.resumeservice.domain.entity.BatchSkill;
import com.luontd.resumeservice.domain.entity.ResumeBatch;
import com.luontd.resumeservice.presentation.dto.BatchSkillRequest;
import com.luontd.resumeservice.presentation.dto.BatchSkillResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchSkillService implements IBatchSkillService {

    private final IBatchSkillRepository batchSkillRepository;
    private final IResumeBatchRepository batchRepository;

    @Override
    @Transactional
    public BatchSkillResponse addSkillToBatch(UUID batchId, BatchSkillRequest request) {
        log.info("Adding skill to batch: {}", batchId);
        ResumeBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with ID: " + batchId));

        BatchSkill skill = BatchSkill.builder()
                .batch(batch)
                .skillName(request.getSkillName())
                .weight(request.getWeight() != null ? request.getWeight() : 1)
                .isMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : false)
                .build();

        BatchSkill savedSkill = batchSkillRepository.save(skill);
        return mapToResponse(savedSkill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchSkillResponse> getSkillsByBatchId(UUID batchId) {
        log.info("Fetching skills for batch: {}", batchId);
        // Verify batch exists
        if (!batchRepository.existsById(batchId)) {
            throw new IllegalArgumentException("Batch not found with ID: " + batchId);
        }
        return batchSkillRepository.findByBatchId(batchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BatchSkillResponse updateBatchSkill(UUID batchId, UUID skillId, BatchSkillRequest request) {
        log.info("Updating skill {} in batch {}", skillId, batchId);
        BatchSkill skill = batchSkillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("BatchSkill not found with ID: " + skillId));

        // Validate that the skill belongs to the given batch
        if (!skill.getBatch().getId().equals(batchId)) {
            throw new IllegalArgumentException("Skill does not belong to the specified batch");
        }

        skill.setSkillName(request.getSkillName());
        skill.setWeight(request.getWeight() != null ? request.getWeight() : 1);
        skill.setIsMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : false);

        BatchSkill updatedSkill = batchSkillRepository.save(skill);
        return mapToResponse(updatedSkill);
    }

    @Override
    @Transactional
    public void removeSkillFromBatch(UUID batchId, UUID skillId) {
        log.info("Removing skill {} from batch {}", skillId, batchId);
        BatchSkill skill = batchSkillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("BatchSkill not found with ID: " + skillId));

        if (!skill.getBatch().getId().equals(batchId)) {
            throw new IllegalArgumentException("Skill does not belong to the specified batch");
        }

        batchSkillRepository.deleteById(skillId);
    }

    private BatchSkillResponse mapToResponse(BatchSkill skill) {
        return BatchSkillResponse.builder()
                .id(skill.getId())
                .batchId(skill.getBatch().getId())
                .skillName(skill.getSkillName())
                .weight(skill.getWeight())
                .isMandatory(skill.getIsMandatory())
                .build();
    }
}
