package com.abdm.hms.service;

import com.abdm.hms.dto.DoctorDto;
import com.abdm.hms.entity.Doctor;
import com.abdm.hms.exception.DuplicateResourceException;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.DoctorRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<DoctorDto> getAll(String search) {
        List<Doctor> doctors = StringUtils.hasText(search)
                ? doctorRepository.search(search.trim())
                : doctorRepository.findAll();
        return doctors.stream().map(this::toDto).toList();
    }

    public DoctorDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public DoctorDto create(DoctorDto dto) {
        if (doctorRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Doctor with email " + dto.getEmail() + " already exists");
        }
        return toDto(doctorRepository.save(toEntity(dto)));
    }

    public DoctorDto update(Long id, DoctorDto dto) {
        Doctor existing = findOrThrow(id);
        if (!dto.getEmail().equals(existing.getEmail()) && doctorRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Doctor with email " + dto.getEmail() + " already exists");
        }
        existing.setFullName(dto.getFullName());
        existing.setSpecialization(dto.getSpecialization());
        existing.setDepartment(dto.getDepartment());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setEmail(dto.getEmail());
        return toDto(doctorRepository.save(existing));
    }

    public void delete(Long id) {
        findOrThrow(id);
        doctorRepository.deleteById(id);
    }

    private Doctor findOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    public DoctorDto toDto(Doctor d) {
        return DoctorDto.builder()
                .doctorId(d.getDoctorId())
                .fullName(d.getFullName())
                .specialization(d.getSpecialization())
                .department(d.getDepartment())
                .phoneNumber(d.getPhoneNumber())
                .email(d.getEmail())
                .build();
    }

    private Doctor toEntity(DoctorDto dto) {
        return Doctor.builder()
                .fullName(dto.getFullName())
                .specialization(dto.getSpecialization())
                .department(dto.getDepartment())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .build();
    }
}
