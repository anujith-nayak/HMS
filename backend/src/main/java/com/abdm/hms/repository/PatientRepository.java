package com.abdm.hms.repository;

import com.abdm.hms.entity.Patient;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByAbhaNumber(String abhaNumber);
    Optional<Patient> findByAbhaNumber(String abhaNumber);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(p.phoneNumber) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(p.abhaNumber) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Patient> search(@Param("q") String query);
}
