package com.nnh.backend.service;

import com.nnh.backend.dto.request.AbsenceRequest;
import com.nnh.backend.dto.response.AbsenceResponse;
import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.AbsenceStatus;
import com.nnh.backend.persistence.entity.Appointment;
import com.nnh.backend.persistence.entity.AppointmentStatus;
import com.nnh.backend.persistence.entity.DoctorAbsence;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.entity.EmployeeRole;
import com.nnh.backend.persistence.repository.AppointmentRepository;
import com.nnh.backend.persistence.repository.DoctorAbsenceRepository;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorAbsenceService {

    private final DoctorAbsenceRepository absenceRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Transactional(readOnly = true)
    public List<AbsenceResponse> getAll() {
        return absenceRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceResponse> getUpcoming() {
        return absenceRepository.findByEndDateGreaterThanEqualAndStatus(LocalDate.now(), AbsenceStatus.APPROVED)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<DoctorAbsence> findAbsence(String doctorId, LocalDate date) {
        return absenceRepository.findFirstByDoctorIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                doctorId, date, date, AbsenceStatus.APPROVED);
    }

    @Transactional
    public AbsenceResponse create(AbsenceRequest req, String creatorEmail) {
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        Employee creator = employeeRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        String message = req.getStartDate().equals(req.getEndDate())
                ? "Dr. Vaishnavi is not present on " + req.getStartDate().format(FMT)
                : "Dr. Vaishnavi is not present from " + req.getStartDate().format(FMT)
                  + " to " + req.getEndDate().format(FMT);

        // Admins auto-approve; care staff requests go to PENDING_APPROVAL
        AbsenceStatus initialStatus = creator.getRole() == EmployeeRole.ADMIN
                ? AbsenceStatus.APPROVED
                : AbsenceStatus.PENDING_APPROVAL;

        DoctorAbsence absence = DoctorAbsence.builder()
                .doctorId(req.getDoctorId())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .message(message)
                .createdBy(creator)
                .status(initialStatus)
                .approvedBy(initialStatus == AbsenceStatus.APPROVED ? creator : null)
                .build();

        log.info("Absence request for {} from {} to {} by {} (status: {})",
                req.getDoctorId(), req.getStartDate(), req.getEndDate(), creatorEmail, initialStatus);
        DoctorAbsence saved = absenceRepository.save(absence);

        if (initialStatus == AbsenceStatus.APPROVED) {
            rescheduleAppointments(req.getDoctorId(), req.getStartDate(), req.getEndDate());
        }

        return toResponse(saved);
    }

    @Transactional
    public AbsenceResponse approve(Long id, String approverEmail) {
        DoctorAbsence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found: " + id));
        Employee approver = employeeRepository.findByEmail(approverEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        absence.setStatus(AbsenceStatus.APPROVED);
        absence.setApprovedBy(approver);
        log.info("Absence {} approved by {}", id, approverEmail);
        return toResponse(absenceRepository.save(absence));
    }

    @Transactional
    public AbsenceResponse reject(Long id) {
        DoctorAbsence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found: " + id));
        absence.setStatus(AbsenceStatus.REJECTED);
        absence.setApprovedBy(null);
        log.info("Absence {} rejected", id);
        return toResponse(absenceRepository.save(absence));
    }

    @Transactional
    public void delete(Long id) {
        if (!absenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Absence record not found with id: " + id);
        }
        absenceRepository.deleteById(id);
        log.info("Deleted absence record {}", id);
    }

    private void rescheduleAppointments(String doctorId, LocalDate absenceStart, LocalDate absenceEnd) {
        List<Appointment> affected = appointmentRepository
                .findByDoctor_IdAndAppointmentDateBetweenAndStatusIn(
                        doctorId, absenceStart, absenceEnd,
                        List.of(AppointmentStatus.PENDING, AppointmentStatus.IN_CONSULTATION));

        if (affected.isEmpty()) return;
        log.info("Rescheduling {} appointment(s) due to absence of {}", affected.size(), doctorId);

        LocalDate candidate = absenceEnd.plusDays(1);
        for (Appointment appt : affected) {
            // Find the first date from candidate onwards where this time slot is free
            LocalDate newDate = candidate;
            while (appointmentRepository.existsByDoctor_IdAndAppointmentDateAndAppointmentTime(
                    doctorId, newDate, appt.getAppointmentTime())) {
                newDate = newDate.plusDays(1);
            }
            log.info("Rescheduling appointment {} from {} to {}", appt.getId(), appt.getAppointmentDate(), newDate);
            appt.setAppointmentDate(newDate);
            appointmentRepository.save(appt);
        }
    }

    private AbsenceResponse toResponse(DoctorAbsence a) {
        return AbsenceResponse.builder()
                .id(a.getId())
                .doctorId(a.getDoctorId())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .message(a.getMessage())
                .status(a.getStatus().name())
                .requestedByName(a.getCreatedBy() != null ? a.getCreatedBy().getName() : null)
                .approvedByName(a.getApprovedBy() != null ? a.getApprovedBy().getName() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
