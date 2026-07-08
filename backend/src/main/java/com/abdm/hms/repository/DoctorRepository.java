package com.abdm.hms.repository;

import com.abdm.hms.entity.Doctor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String email);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(d.department) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Doctor> search(@Param("q") String query);
}
