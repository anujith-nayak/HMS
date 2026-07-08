package com.abdm.hms.repository;

import com.abdm.hms.entity.Observation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ObservationRepository extends JpaRepository<Observation, Long> {
    List<Observation> findByPatient_PatientId(Long patientId);

    @Query("SELECT o FROM Observation o JOIN o.patient p " +
           "WHERE LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(o.bloodPressure) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Observation> search(@Param("q") String query);
}
