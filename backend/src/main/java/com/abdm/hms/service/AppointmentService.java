package com.abdm.hms.service;

import com.abdm.hms.dto.AppointmentDto;
import com.abdm.hms.entity.Appointment;
import com.abdm.hms.entity.Doctor;
import com.abdm.hms.entity.Patient;
import com.abdm.hms.exception.ResourceNotFoundException;
import com.abdm.hms.repository.AppointmentRepository;
import com.abdm.hms.repository.DoctorRepository;
import com.abdm.hms.repository.PatientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public List<AppointmentDto> getAll(String search) {
        List<Appointment> list = StringUtils.hasText(search)
                ? appointmentRepository.search(search.trim())
                : appointmentRepository.findAll();
        return list.stream().map(this::toDto).toList();
    }

    public AppointmentDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    public AppointmentDto create(AppointmentDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.getDoctorId()));
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(dto.getAppointmentTime())
                .status(dto.getStatus())
                .build();
        return toDto(appointmentRepository.save(appointment));
    }

    public AppointmentDto update(Long id, AppointmentDto dto) {
        Appointment existing = findOrThrow(id);
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.getPatientId()));
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.getDoctorId()));
        existing.setPatient(patient);
        existing.setDoctor(doctor);
        existing.setAppointmentDate(dto.getAppointmentDate());
        existing.setAppointmentTime(dto.getAppointmentTime());
        existing.setStatus(dto.getStatus());
        return toDto(appointmentRepository.save(existing));
    }

    public void delete(Long id) {
        findOrThrow(id);
        appointmentRepository.deleteById(id);
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    private AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .appointmentId(a.getAppointmentId())
                .patientId(a.getPatient().getPatientId())
                .patientName(a.getPatient().getFullName())
                .doctorId(a.getDoctor().getDoctorId())
                .doctorName(a.getDoctor().getFullName())
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .status(a.getStatus())
                .build();
    }
}
