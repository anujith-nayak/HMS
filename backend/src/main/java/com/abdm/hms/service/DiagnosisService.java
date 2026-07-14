package com.abdm.hms.service;

import com.abdm.hms.dto.DiagnosisDto;
import com.abdm.hms.entity.Diagnosis;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.DiagnosisRepository;
import com.abdm.hms.repository.PatientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final EHRBaseService ehrBaseService;

    public List<DiagnosisDto> getByPatientId(Long patientId) {
        return diagnosisRepository.findByPatientPatientIdOrderByDiagnosisDateDesc(patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public DiagnosisDto create(DiagnosisDto dto) {

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new IllegalStateException("Patient has no EHR ID for diagnosis submission");
        }

        ehrBaseService.saveDiagnosis(patient.getPatientId(), dto);

        Diagnosis diagnosis = toEntity(dto);
        diagnosis.setPatient(patient);

        return toDto(diagnosisRepository.save(diagnosis));
    }

    private DiagnosisDto toDto(Diagnosis diagnosis) {
        return DiagnosisDto.builder()
                .diagnosisId(diagnosis.getDiagnosisId())
                .patientId(diagnosis.getPatient().getPatientId())
                .diagnosisName(diagnosis.getDiagnosisName())
                .diagnosisCode(diagnosis.getDiagnosisCode())
                .description(diagnosis.getDescription())
                .severity(diagnosis.getSeverity())
                .doctorName(diagnosis.getDoctorName())
                .diagnosisDate(diagnosis.getDiagnosisDate())
                .notes(diagnosis.getNotes())
                .createdAt(diagnosis.getCreatedAt())
                .build();
    }

    private Diagnosis toEntity(DiagnosisDto dto) {
        return Diagnosis.builder()
                .diagnosisName(dto.getDiagnosisName())
                .diagnosisCode(dto.getDiagnosisCode())
                .description(dto.getDescription())
                .severity(dto.getSeverity())
                .doctorName(dto.getDoctorName())
                .diagnosisDate(dto.getDiagnosisDate())
                .notes(dto.getNotes())
                .build();
    }
}
