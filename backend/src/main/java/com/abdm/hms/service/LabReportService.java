package com.abdm.hms.service;

import com.abdm.hms.dto.LabReportDto;
import com.abdm.hms.entity.LabReport;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.LabReportRepository;
import com.abdm.hms.repository.PatientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class LabReportService {

    private final LabReportRepository labReportRepository;
    private final PatientRepository patientRepository;
    private final EHRBaseService ehrBaseService;

    public List<LabReportDto> getByPatientId(Long patientId) {
        return labReportRepository.findByPatientPatientIdOrderByReportDateDesc(patientId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public LabReportDto create(LabReportDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));

        if (!StringUtils.hasText(patient.getEhrId())) {
            throw new IllegalStateException("Patient has no EHR ID for lab report submission");
        }

        ehrBaseService.saveLabReport(patient.getPatientId(), dto);

        LabReport labReport = toEntity(dto);
        labReport.setPatient(patient);

        return toDto(labReportRepository.save(labReport));
    }

    private LabReportDto toDto(LabReport report) {
        return LabReportDto.builder()
                .labReportId(report.getLabReportId())
                .patientId(report.getPatient().getPatientId())
                .testName(report.getTestName())
                .testCode(report.getTestCode())
                .result(report.getResult())
                .unit(report.getUnit())
                .referenceRange(report.getReferenceRange())
                .status(report.getStatus())
                .reportDate(report.getReportDate())
                .remarks(report.getRemarks())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private LabReport toEntity(LabReportDto dto) {
        return LabReport.builder()
                .testName(dto.getTestName())
                .testCode(dto.getTestCode())
                .result(dto.getResult())
                .unit(dto.getUnit())
                .referenceRange(dto.getReferenceRange())
                .status(dto.getStatus())
                .reportDate(dto.getReportDate())
                .remarks(dto.getRemarks())
                .build();
    }
}
