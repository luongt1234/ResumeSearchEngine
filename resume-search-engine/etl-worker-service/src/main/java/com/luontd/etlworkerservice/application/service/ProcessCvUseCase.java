package com.luontd.etlworkerservice.application.service;

import com.luontd.etlworkerservice.application.dto.ResumeEventDto;
import com.luontd.etlworkerservice.application.port.in.IProcessCvUseCase;
import com.luontd.etlworkerservice.application.port.out.IFileStoragePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessCvUseCase implements IProcessCvUseCase {
    private IFileStoragePort _fileStoragePort;

    @Override
    public void Execute(ResumeEventDto event) {
        try{
            // get file trong minio
            String fileUrl = event.getFileUrl();
            var fileByte = _fileStoragePort.GetFileByFileUrl(fileUrl);

            if (fileByte == null || fileByte.getFileBytes().length == 0){
                throw new RuntimeException("File is not exsit or length is unsatisfied");
            }


        } catch (Exception ex) {
            throw new RuntimeException("");
        }
    }
}
