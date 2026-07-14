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
public class DischargeSummaryDto {

    private Long dischargeSummaryId;

    @NotNull
    private Long patientId;

    @Size(max = 255)
    private String finalDiagnosis;

    @Size(max = 2000)
    private String clinicalSummary;

    @Size(max = 2000)
    private String treatment;

    @Size(max = 2000)
    private String advice;

    @NotNull
    private LocalDate dischargeDate;

    @NotNull
    private LocalDate followUpDate;

    private LocalDateTime createdAt;
}
