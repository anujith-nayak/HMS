package com.abdm.hms.service;

import com.abdm.hms.dto.DischargeSummaryDto;
import com.abdm.hms.entity.DischargeSummary;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.DischargeSummaryRepository;
import com.abdm.hms.repository.PatientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class DischargeSummaryService {

    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final PatientRepository patientRepository;
    private final EHRBaseService ehrBaseService;

    public List<DischargeSummaryDto> getByPatientId(Long patientId) {
        return dischargeSummaryRepository.findByPatientPatientIdOrderByDischargeDateDesc(patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public DischargeSummaryDto create(DischargeSummaryDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new IllegalStateException("Patient has no EHR ID for discharge summary submission");
        }

        ehrBaseService.saveDischargeSummary(patient.getPatientId(), dto);

        DischargeSummary summary = toEntity(dto);
        summary.setPatient(patient);

        return toDto(dischargeSummaryRepository.save(summary));
    }

    private DischargeSummaryDto toDto(DischargeSummary summary) {
        return DischargeSummaryDto.builder()
                .dischargeSummaryId(summary.getDischargeSummaryId())
                .patientId(summary.getPatient().getPatientId())
                .finalDiagnosis(summary.getFinalDiagnosis())
                .clinicalSummary(summary.getClinicalSummary())
                .treatment(summary.getTreatment())
                .advice(summary.getAdvice())
                .dischargeDate(summary.getDischargeDate())
                .followUpDate(summary.getFollowUpDate())
                .createdAt(summary.getCreatedAt())
                .build();
    }

    private DischargeSummary toEntity(DischargeSummaryDto dto) {
        return DischargeSummary.builder()
                .finalDiagnosis(dto.getFinalDiagnosis())
                .clinicalSummary(dto.getClinicalSummary())
                .treatment(dto.getTreatment())
                .advice(dto.getAdvice())
                .dischargeDate(dto.getDischargeDate())
                .followUpDate(dto.getFollowUpDate())
                .build();
    }
}
