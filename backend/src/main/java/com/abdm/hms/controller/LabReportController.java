package com.abdm.hms.controller;

import com.abdm.hms.dto.LabReportDto;
import com.abdm.hms.service.LabReportService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab-reports")
@RequiredArgsConstructor
public class LabReportController {

    private final LabReportService labReportService;

    @GetMapping("/patient/{patientId}")
    public List<LabReportDto> getByPatientId(@PathVariable Long patientId) {
        return labReportService.getByPatientId(patientId);
    }

    @PostMapping
    public ResponseEntity<LabReportDto> create(@Valid @RequestBody LabReportDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labReportService.create(dto));
    }
}
