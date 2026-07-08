package com.abdm.hms.controller;

import com.abdm.hms.dto.PrescriptionDto;
import com.abdm.hms.service.PrescriptionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    public List<PrescriptionDto> getAll(@RequestParam(required = false) String search) {
        return prescriptionService.getAll(search);
    }

    @GetMapping("/{id}")
    public PrescriptionDto getById(@PathVariable Long id) {
        return prescriptionService.getById(id);
    }

    @PostMapping
    public ResponseEntity<PrescriptionDto> create(@Valid @RequestBody PrescriptionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(dto));
    }

    @PutMapping("/{id}")
    public PrescriptionDto update(@PathVariable Long id, @Valid @RequestBody PrescriptionDto dto) {
        return prescriptionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
