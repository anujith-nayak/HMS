package com.abdm.hms.controller;

import com.abdm.hms.dto.DiagnosisDto;
import com.abdm.hms.service.DiagnosisService;
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
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @GetMapping("/patient/{patientId}")
    public List<DiagnosisDto> getByPatientId(@PathVariable Long patientId) {
        return diagnosisService.getByPatientId(patientId);
    }

    @PostMapping
    public ResponseEntity<DiagnosisDto> create(@Valid @RequestBody DiagnosisDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosisService.create(dto));
    }
}
