package com.luontd.resumeservice.application.services;

import com.luontd.resumeservice.application.interfaces.usecase.IResumeBatchService;
import com.luontd.resumeservice.application.interfaces.repository.IResumeBatchRepository;
import com.luontd.resumeservice.domain.entity.ResumeBatch;
import com.luontd.resumeservice.presentation.dto.ResumeBatchRequest;
import com.luontd.resumeservice.presentation.dto.ResumeBatchResponse;
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
public class ResumeBatchService implements IResumeBatchService {

    private final IResumeBatchRepository batchRepository;

    @Override
    @Transactional
    public ResumeBatchResponse createBatch(ResumeBatchRequest request) {
        log.info("Creating new ResumeBatch: {}", request.getBatchName());
        ResumeBatch batch = ResumeBatch.builder()
                .batchName(request.getBatchName())
                .targetPosition(request.getTargetPosition())
                .minYoe(request.getMinYoe())
                .description(request.getDescription())
                .build();
        
        ResumeBatch savedBatch = batchRepository.save(batch);
        var result = mapToResponse(savedBatch);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeBatchResponse getBatchById(UUID id) {
        log.info("Fetching ResumeBatch with id: {}", id);
        ResumeBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with ID: " + id));
        return mapToResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeBatchResponse> getAllBatches() {
        log.info("Fetching all ResumeBatches");
        return batchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResumeBatchResponse updateBatch(UUID id, ResumeBatchRequest request) {
        log.info("Updating ResumeBatch with id: {}", id);
        ResumeBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found with ID: " + id));
        
        batch.setBatchName(request.getBatchName());
        batch.setTargetPosition(request.getTargetPosition());
        batch.setMinYoe(request.getMinYoe());
        batch.setDescription(request.getDescription());
        
        ResumeBatch updatedBatch = batchRepository.save(batch);
        return mapToResponse(updatedBatch);
    }

    @Override
    @Transactional
    public void deleteBatch(UUID id) {
        log.info("Deleting ResumeBatch with id: {}", id);
        if (!batchRepository.existsById(id)) {
            throw new IllegalArgumentException("Batch not found with ID: " + id);
        }
        batchRepository.deleteById(id);
    }

    private ResumeBatchResponse mapToResponse(ResumeBatch batch) {
        return ResumeBatchResponse.builder()
                .id(batch.getId())
                .batchName(batch.getBatchName())
                .targetPosition(batch.getTargetPosition())
                .minYoe(batch.getMinYoe())
                .description(batch.getDescription())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }
}
