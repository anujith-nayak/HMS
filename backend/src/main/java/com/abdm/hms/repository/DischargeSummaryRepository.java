package com.abdm.hms.repository;

import com.abdm.hms.entity.DischargeSummary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, Long> {
    List<DischargeSummary> findByPatientPatientIdOrderByDischargeDateDesc(Long patientId);
}
