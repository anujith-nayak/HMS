package com.abdm.hms.repository;

import com.abdm.hms.entity.Appointment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient_PatientId(Long patientId);
    List<Appointment> findByDoctor_DoctorId(Long doctorId);

    @Query("SELECT a FROM Appointment a JOIN a.patient p JOIN a.doctor d " +
           "WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(a.status) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Appointment> search(@Param("q") String query);
}
