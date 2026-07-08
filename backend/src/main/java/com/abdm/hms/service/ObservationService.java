package com.abdm.hms.service;

import com.abdm.hms.dto.ObservationDto;
import com.abdm.hms.entity.Observation;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.ObservationRepository;
import com.abdm.hms.repository.PatientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class ObservationService {

    private final ObservationRepository observationRepository;
    private final PatientRepository patientRepository;

    public List<ObservationDto> getAll(String search) {
        List<Observation> list = StringUtils.hasText(search)
                ? observationRepository.search(search.trim())
                : observationRepository.findAll();
        return list.stream().map(this::toDto).toList();
    }

    public ObservationDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public ObservationDto create(ObservationDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));
        Observation obs = Observation.builder()
                .patient(patient)
                .bloodPressure(dto.getBloodPressure())
                .temperature(dto.getTemperature())
                .heartRate(dto.getHeartRate())
                .weight(dto.getWeight())
                .height(dto.getHeight())
                .recordedDate(dto.getRecordedDate())
                .build();
        return toDto(observationRepository.save(obs));
    }

    public ObservationDto update(Long id, ObservationDto dto) {
        Observation existing = findOrThrow(id);
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));
        existing.setPatient(patient);
        existing.setBloodPressure(dto.getBloodPressure());
        existing.setTemperature(dto.getTemperature());
        existing.setHeartRate(dto.getHeartRate());
        existing.setWeight(dto.getWeight());
        existing.setHeight(dto.getHeight());
        existing.setRecordedDate(dto.getRecordedDate());
        return toDto(observationRepository.save(existing));
    }

    public void delete(Long id) {
        findOrThrow(id);
        observationRepository.deleteById(id);
    }

    private Observation findOrThrow(Long id) {
        return observationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Observation", id));
    }

    private ObservationDto toDto(Observation o) {
        return ObservationDto.builder()
                .observationId(o.getObservationId())
                .patientId(o.getPatient().getPatientId())
                .patientName(o.getPatient().getFullName())
                .bloodPressure(o.getBloodPressure())
                .temperature(o.getTemperature())
                .heartRate(o.getHeartRate())
                .weight(o.getWeight())
                .height(o.getHeight())
                .recordedDate(o.getRecordedDate())
                .build();
    }
}
