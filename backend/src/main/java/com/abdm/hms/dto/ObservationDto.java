package com.abdm.hms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationDto {
    private Long observationId;

    @NotNull
    private Long patientId;
    private String patientName;

    @NotBlank
    @Size(max = 30)
    private String bloodPressure;

    @NotNull
    @DecimalMin("30.0")
    @DecimalMax("45.0")
    private BigDecimal temperature;

    @NotNull
    @Min(30)
    @Max(300)
    private Integer heartRate;

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("500.0")
    private BigDecimal weight;

    @NotNull
    @DecimalMin("30.0")
    @DecimalMax("300.0")
    private BigDecimal height;

    @NotNull
    @PastOrPresent
    private LocalDate recordedDate;
}
