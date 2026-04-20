package com.nnh.backend.service;

import com.nnh.backend.dto.response.AuditEntryResponse;
import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.Appointment;
import com.nnh.backend.persistence.entity.AppointmentAudit;
import com.nnh.backend.persistence.entity.AppointmentStatus;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.repository.AppointmentAuditRepository;
import com.nnh.backend.persistence.repository.AppointmentRepository;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentAuditRepository auditRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Appointment create(Appointment appointment) {
        log.info("Persisting appointment for {} {}", appointment.getFirstName(), appointment.getLastName());
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment getById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAll() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByEmail(String email) {
        return appointmentRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByDoctorAndDate(String doctorId, LocalDate date) {
        return appointmentRepository.findByDoctor_IdAndAppointmentDate(doctorId, date);
    }

    @Transactional(readOnly = true)
    public boolean isSlotAvailable(String doctorId, LocalDate date, String time) {
        return !appointmentRepository.existsByDoctor_IdAndAppointmentDateAndAppointmentTime(doctorId, date, time);
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus newStatus, String changedByEmail) {
        Appointment appointment = getById(id);
        String oldStatus = appointment.getStatus().name();

        log.info("Appointment {}: {} → {} by {}", id, oldStatus, newStatus, changedByEmail);
        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appointment);

        // Resolve employee name for the audit record
        String changedByName = employeeRepository.findByEmail(changedByEmail)
                .map(Employee::getName)
                .orElse(changedByEmail);

        auditRepository.save(AppointmentAudit.builder()
                .appointmentId(id)
                .changedByName(changedByName)
                .changedByEmail(changedByEmail)
                .oldStatus(oldStatus)
                .newStatus(newStatus.name())
                .build());

        return saved;
    }

    @Transactional
    public void recordCreationAudit(Long appointmentId, String createdByEmail, String initialStatus) {
        String createdByName = employeeRepository.findByEmail(createdByEmail)
                .map(Employee::getName)
                .orElse(createdByEmail);

        auditRepository.save(AppointmentAudit.builder()
                .appointmentId(appointmentId)
                .changedByName(createdByName)
                .changedByEmail(createdByEmail)
                .oldStatus("—")
                .newStatus(initialStatus)
                .build());
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> getAuditLog(Long appointmentId) {
        return auditRepository.findByAppointmentIdOrderByChangedAtDesc(appointmentId)
                .stream()
                .map(a -> AuditEntryResponse.builder()
                        .id(a.getId())
                        .changedByName(a.getChangedByName())
                        .changedByEmail(a.getChangedByEmail())
                        .oldStatus(a.getOldStatus())
                        .newStatus(a.getNewStatus())
                        .changedAt(a.getChangedAt())
                        .build())
                .toList();
    }
}
