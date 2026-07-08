package com.abdm.hms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
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
public class PatientDto {

    private Long patientId;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Size(max = 30)
    private String gender;

    @NotNull
    @Past
    private LocalDate dateOfBirth;

    @NotBlank
    @Pattern(
        regexp = "^[0-9+\\-\\s]{7,20}$",
        message = "must be a valid phone number"
    )
    private String phoneNumber;

    @NotBlank
    @Size(max = 255)
    private String address;

    @Size(max = 30)
    private String abhaNumber;

    // EHRbase EHR ID
    private String ehrId;
}