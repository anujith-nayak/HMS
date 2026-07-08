package com.abdm.hms.repository;

import com.abdm.hms.entity.Prescription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatient_PatientId(Long patientId);

    @Query("SELECT pr FROM Prescription pr JOIN pr.patient p JOIN pr.doctor d " +
           "WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(pr.medicineName) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Prescription> search(@Param("q") String query);
}
