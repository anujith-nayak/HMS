package com.abdm.hms.repository;

import com.abdm.hms.entity.Patient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByAbhaNumber(String abhaNumber);

    Optional<Patient> findById(Long patientId);

    @Query("""
        SELECT p
        FROM Patient p
        WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(p.abhaNumber,'')) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    List<Patient> search(String search);
}