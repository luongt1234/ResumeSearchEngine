package com.luontd.etlworkerservice.application.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDto {
    private String school;
    private String degree;
    private String major;
    private String year;
}
