package com.abdm.hms.controller;

import com.abdm.hms.dto.DischargeSummaryDto;
import com.abdm.hms.service.DischargeSummaryService;
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
@RequestMapping("/api/discharge-summaries")
@RequiredArgsConstructor
public class DischargeSummaryController {

    private final DischargeSummaryService dischargeSummaryService;

    @GetMapping("/patient/{patientId}")
    public List<DischargeSummaryDto> getByPatientId(@PathVariable Long patientId) {
        return dischargeSummaryService.getByPatientId(patientId);
    }

    @PostMapping
    public ResponseEntity<DischargeSummaryDto> create(@Valid @RequestBody DischargeSummaryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dischargeSummaryService.create(dto));
    }
}
