package com.luontd.resumeservice.application.interfaces.usecase;

import com.luontd.resumeservice.application.dto.ScoreResultDto;

import java.util.UUID;

public interface IScoreResumeService {
    ScoreResultDto scoreBatch(UUID batchId);
}
