package com.abdm.hms.service;

import com.abdm.hms.dto.PrescriptionDto;
import com.abdm.hms.entity.Doctor;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.entity.Prescription;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.DoctorRepository;
import com.abdm.hms.repository.PatientRepository;
import com.abdm.hms.repository.PrescriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public List<PrescriptionDto> getAll(String search) {
        List<Prescription> list = StringUtils.hasText(search)
                ? prescriptionRepository.search(search.trim())
                : prescriptionRepository.findAll();
        return list.stream().map(this::toDto).toList();
    }

    public PrescriptionDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public PrescriptionDto create(PrescriptionDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.getDoctorId()));
        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medicineName(dto.getMedicineName())
                .dosage(dto.getDosage())
                .instructions(dto.getInstructions())
                .prescribedDate(dto.getPrescribedDate())
                .build();
        return toDto(prescriptionRepository.save(prescription));
    }

    public PrescriptionDto update(Long id, PrescriptionDto dto) {
        Prescription existing = findOrThrow(id);
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.getDoctorId()));
        existing.setPatient(patient);
        existing.setDoctor(doctor);
        existing.setMedicineName(dto.getMedicineName());
        existing.setDosage(dto.getDosage());
        existing.setInstructions(dto.getInstructions());
        existing.setPrescribedDate(dto.getPrescribedDate());
        return toDto(prescriptionRepository.save(existing));
    }

    public void delete(Long id) {
        findOrThrow(id);
        prescriptionRepository.deleteById(id);
    }

    private Prescription findOrThrow(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
    }

    private PrescriptionDto toDto(Prescription p) {
        return PrescriptionDto.builder()
                .prescriptionId(p.getPrescriptionId())
                .patientId(p.getPatient().getPatientId())
                .patientName(p.getPatient().getFullName())
                .doctorId(p.getDoctor().getDoctorId())
                .doctorName(p.getDoctor().getFullName())
                .medicineName(p.getMedicineName())
                .dosage(p.getDosage())
                .instructions(p.getInstructions())
                .prescribedDate(p.getPrescribedDate())
                .build();
    }
}
