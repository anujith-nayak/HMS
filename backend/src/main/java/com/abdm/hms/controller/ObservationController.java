package com.abdm.hms.controller;

import com.abdm.hms.dto.ObservationDto;
import com.abdm.hms.service.ObservationService;
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
@RequestMapping("/api/observations")
@RequiredArgsConstructor
public class ObservationController {

    private final ObservationService observationService;

    @GetMapping
    public List<ObservationDto> getAll(@RequestParam(required = false) String search) {
        return observationService.getAll(search);
    }

    @GetMapping("/{id}")
    public ObservationDto getById(@PathVariable Long id) {
        return observationService.getById(id);
    }

    @PostMapping
    public ResponseEntity<ObservationDto> create(@Valid @RequestBody ObservationDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(observationService.create(dto));
    }

    @PutMapping("/{id}")
    public ObservationDto update(@PathVariable Long id, @Valid @RequestBody ObservationDto dto) {
        return observationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        observationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
