package com.abdm.hms.repository;

import com.abdm.hms.entity.Diagnosis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findByPatientPatientIdOrderByDiagnosisDateDesc(Long patientId);
}
