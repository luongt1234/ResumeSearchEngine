package com.luontd.etlworkerservice.application.port.in;

import com.luontd.etlworkerservice.application.dto.ResumeEventDto;

public interface IProcessCvUseCase {
    void Execute(ResumeEventDto event);
}
