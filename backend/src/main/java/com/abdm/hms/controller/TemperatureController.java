package com.abdm.hms.controller;

import com.abdm.hms.dto.VitalSignsDto;
import com.abdm.hms.service.EHRBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vitals")
@RequiredArgsConstructor
public class TemperatureController {

    private final EHRBaseService ehrBaseService;

    @PostMapping("/{patientId}")
    public String saveVitals(
            @PathVariable Long patientId,
            @RequestBody VitalSignsDto dto) {

        return ehrBaseService.saveVitals(patientId, dto);
    }
}