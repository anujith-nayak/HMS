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
public class DiagnosisDto {

    private Long diagnosisId;

    @NotNull
    private Long patientId;

    @NotBlank
    @Size(max = 120)
    private String diagnosisName;

    @Size(max = 50)
    private String diagnosisCode;

    @Size(max = 255)
    private String description;

    @Size(max = 50)
    private String severity;

    @Size(max = 120)
    private String doctorName;

    @NotNull
    private LocalDate diagnosisDate;

    @Size(max = 1000)
    private String notes;

    private LocalDateTime createdAt;
}
