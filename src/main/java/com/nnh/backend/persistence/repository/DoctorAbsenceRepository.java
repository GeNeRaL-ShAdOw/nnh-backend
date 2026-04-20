package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.AbsenceStatus;
import com.nnh.backend.persistence.entity.DoctorAbsence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAbsenceRepository extends JpaRepository<DoctorAbsence, Long> {

    Optional<DoctorAbsence> findFirstByDoctorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            String doctorId, LocalDate startDate, LocalDate endDate, AbsenceStatus status);

    List<DoctorAbsence> findByEndDateGreaterThanEqualAndStatus(LocalDate date, AbsenceStatus status);

    List<DoctorAbsence> findAllByOrderByCreatedAtDesc();
}
