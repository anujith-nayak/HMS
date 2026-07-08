package com.abdm.hms.service;

import com.abdm.hms.dto.PatientDto;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.exception.DuplicateResourceException;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.PatientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public List<PatientDto> getAll(String search) {
        List<Patient> patients = StringUtils.hasText(search)
                ? patientRepository.search(search.trim())
                : patientRepository.findAll();
        return patients.stream().map(this::toDto).toList();
    }

    public PatientDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public PatientDto create(PatientDto dto) {
        // treat empty string as null so the unique constraint allows multiple patients without ABHA
        if (!StringUtils.hasText(dto.getAbhaNumber())) dto.setAbhaNumber(null);
        if (dto.getAbhaNumber() != null && patientRepository.existsByAbhaNumber(dto.getAbhaNumber())) {
            throw new DuplicateResourceException("Patient with ABHA number " + dto.getAbhaNumber() + " already exists");
        }
        Patient saved = patientRepository.save(toEntity(dto));
        return toDto(saved);
    }

    public PatientDto update(Long id, PatientDto dto) {
        Patient existing = findOrThrow(id);
        // treat empty string as null
        if (!StringUtils.hasText(dto.getAbhaNumber())) dto.setAbhaNumber(null);
        if (dto.getAbhaNumber() != null &&
                !dto.getAbhaNumber().equals(existing.getAbhaNumber()) &&
                patientRepository.existsByAbhaNumber(dto.getAbhaNumber())) {
            throw new DuplicateResourceException("Patient with ABHA number " + dto.getAbhaNumber() + " already exists");
        }
        existing.setFullName(dto.getFullName());
        existing.setGender(dto.getGender());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setAddress(dto.getAddress());
        existing.setAbhaNumber(dto.getAbhaNumber());
        return toDto(patientRepository.save(existing));
    }

    public void delete(Long id) {
        findOrThrow(id);
        patientRepository.deleteById(id);
    }

    private Patient findOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    public PatientDto toDto(Patient p) {
        return PatientDto.builder()
                .patientId(p.getPatientId())
                .fullName(p.getFullName())
                .gender(p.getGender())
                .dateOfBirth(p.getDateOfBirth())
                .phoneNumber(p.getPhoneNumber())
                .address(p.getAddress())
                .abhaNumber(p.getAbhaNumber())
                .build();
    }

    private Patient toEntity(PatientDto dto) {
        return Patient.builder()
                .fullName(dto.getFullName())
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .abhaNumber(dto.getAbhaNumber())
                .build();
    }
}
