package com.abdm.hms.service;

import com.abdm.hms.dto.DashboardStatsDto;
import com.abdm.hms.repository.AppointmentRepository;
import com.abdm.hms.repository.DoctorRepository;
import com.abdm.hms.repository.ObservationRepository;
import com.abdm.hms.repository.PatientRepository;
import com.abdm.hms.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final ObservationRepository observationRepository;

    public DashboardStatsDto getStats() {
        return DashboardStatsDto.builder()
                .totalPatients(patientRepository.count())
                .totalDoctors(doctorRepository.count())
                .totalAppointments(appointmentRepository.count())
                .totalPrescriptions(prescriptionRepository.count())
                .totalObservations(observationRepository.count())
                .build();
    }
}
