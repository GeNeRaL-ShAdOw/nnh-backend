package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.Appointment;
import com.nnh.backend.persistence.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Used by the orchestrator to enforce one-appointment-per-slot per doctor.
     * Property traversal: doctor → id via Spring Data naming convention.
     */
    boolean existsByDoctor_IdAndAppointmentDateAndAppointmentTime(
        String doctorId, LocalDate appointmentDate, String appointmentTime);

    List<Appointment> findByEmail(String email);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByDoctor_IdAndAppointmentDate(String doctorId, LocalDate date);

    List<Appointment> findByDoctor_IdAndAppointmentDateBetweenAndStatusIn(
        String doctorId, LocalDate from, LocalDate to, List<AppointmentStatus> statuses);
}
