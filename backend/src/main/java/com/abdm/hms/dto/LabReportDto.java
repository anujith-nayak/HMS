package com.abdm.hms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabReportDto {

    private Long labReportId;

    @NotNull
    private Long patientId;

    @NotBlank
    @Size(max = 120)
    private String testName;

    @Size(max = 120)
    private String testCode;

    @Size(max = 255)
    private String result;

    @Size(max = 120)
    private String unit;

    @Size(max = 120)
    private String referenceRange;

    @Size(max = 120)
    private String status;

    @NotNull
    private LocalDate reportDate;

    @Size(max = 1000)
    private String remarks;

    private LocalDateTime createdAt;
}
