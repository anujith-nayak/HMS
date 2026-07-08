package com.abdm.hms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignsDto {

    private Double temperature;

    private Integer systolic;

    private Integer diastolic;

    private Double weight;

    private Double height;

    private Double bmi;

    private Integer pulse;

    private Integer respiration;

    private Integer spo2;
}