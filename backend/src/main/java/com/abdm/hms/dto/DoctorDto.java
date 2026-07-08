package com.abdm.hms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {
    private Long doctorId;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @NotBlank
    @Size(max = 120)
    private String specialization;

    @NotBlank
    @Size(max = 120)
    private String department;

    @NotBlank
    @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$", message = "must be a valid phone number")
    private String phoneNumber;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;
}
