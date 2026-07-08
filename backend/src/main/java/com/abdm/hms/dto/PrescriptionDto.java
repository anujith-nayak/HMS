package com.abdm.hms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDto {
    private Long prescriptionId;

    @NotNull
    private Long patientId;
    private String patientName;

    @NotNull
    private Long doctorId;
    private String doctorName;

    @NotBlank
    @Size(max = 160)
    private String medicineName;

    @NotBlank
    @Size(max = 120)
    private String dosage;

    @Size(max = 500)
    private String instructions;

    @NotNull
    @PastOrPresent
    private LocalDate prescribedDate;
}
