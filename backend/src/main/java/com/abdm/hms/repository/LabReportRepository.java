package com.abdm.hms.repository;

import com.abdm.hms.entity.LabReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    List<LabReport> findByPatientPatientIdOrderByReportDateDesc(Long patientId);
}
