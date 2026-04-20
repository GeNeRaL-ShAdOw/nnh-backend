package com.nnh.backend.orchestration;

import com.nnh.backend.dto.request.AppointmentRequest;
import com.nnh.backend.dto.response.AppointmentResponse;
import com.nnh.backend.dto.response.AuditEntryResponse;
import com.nnh.backend.exception.AppointmentConflictException;
import com.nnh.backend.persistence.entity.Appointment;
import com.nnh.backend.persistence.entity.AppointmentStatus;
import com.nnh.backend.persistence.entity.Doctor;
import com.nnh.backend.persistence.entity.MedicalService;
import com.nnh.backend.service.AppointmentService;
import com.nnh.backend.service.DoctorAbsenceService;
import com.nnh.backend.service.DoctorService;
import com.nnh.backend.service.MedicalServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentOrchestrator {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;
    private final MedicalServiceService medicalServiceService;
    private final DoctorAbsenceService doctorAbsenceService;

    // ── Public booking (status = PENDING) ────────────────────────────────────

    public AppointmentResponse book(AppointmentRequest req) {
        return bookWithStatus(req, AppointmentStatus.PENDING);
    }

    // ── Staff ad-hoc booking (status = CONFIRMED) ─────────────────────────────

    public AppointmentResponse bookAsStaff(AppointmentRequest req, String createdByEmail) {
        AppointmentResponse response = bookWithStatus(req, AppointmentStatus.PENDING);
        appointmentService.recordCreationAudit(response.getId(), createdByEmail, AppointmentStatus.PENDING.name());
        return response;
    }

    // ── Shared booking logic ──────────────────────────────────────────────────

    private AppointmentResponse bookWithStatus(AppointmentRequest req, AppointmentStatus initialStatus) {
        log.info("Booking appointment [{}}] for {} {} on {} at {}",
            initialStatus, req.getFirstName(), req.getLastName(),
            req.getAppointmentDate(), req.getAppointmentTime());

        Doctor doctor = doctorService.getById(req.getDoctorId());
        MedicalService service = medicalServiceService.getById(req.getServiceId());

        doctorAbsenceService.findAbsence(req.getDoctorId(), req.getAppointmentDate())
                .ifPresent(absence -> {
                    throw new AppointmentConflictException("Cannot book: " + absence.getMessage());
                });

        if (!appointmentService.isSlotAvailable(req.getDoctorId(), req.getAppointmentDate(), req.getAppointmentTime())) {
            throw new AppointmentConflictException(
                "The " + req.getAppointmentTime() + " slot on " + req.getAppointmentDate()
                    + " with " + doctor.getName() + " is already taken. Please choose a different time.");
        }

        Appointment saved = appointmentService.create(
            Appointment.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .dateOfBirth(req.getDateOfBirth())
                .newPatient(req.getNewPatient())
                .doctor(doctor)
                .service(service)
                .reason(req.getReason())
                .appointmentDate(req.getAppointmentDate())
                .appointmentTime(req.getAppointmentTime())
                .status(initialStatus)
                .build()
        );

        log.info("Appointment {} created ({}) for {}", saved.getId(), initialStatus, saved.getEmail());
        return toResponse(saved);
    }

    // ── Other operations ──────────────────────────────────────────────────────

    public AppointmentResponse getById(Long id) {
        return toResponse(appointmentService.getById(id));
    }

    public List<AppointmentResponse> getAll() {
        return appointmentService.getAll().stream().map(this::toResponse).toList();
    }

    public List<AppointmentResponse> getByEmail(String email) {
        return appointmentService.getByEmail(email).stream().map(this::toResponse).toList();
    }

    public AppointmentResponse updateStatus(Long id, AppointmentStatus status, String changedByEmail) {
        return toResponse(appointmentService.updateStatus(id, status, changedByEmail));
    }

    public List<AuditEntryResponse> getAuditLog(Long appointmentId) {
        return appointmentService.getAuditLog(appointmentId);
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private AppointmentResponse toResponse(Appointment a) {
        return AppointmentResponse.builder()
            .id(a.getId())
            .firstName(a.getFirstName())
            .lastName(a.getLastName())
            .email(a.getEmail())
            .phone(a.getPhone())
            .dateOfBirth(a.getDateOfBirth())
            .newPatient(a.getNewPatient())
            .doctorId(a.getDoctor().getId())
            .doctorName(a.getDoctor().getName())
            .serviceId(a.getService().getId())
            .serviceName(a.getService().getTitle())
            .reason(a.getReason())
            .appointmentDate(a.getAppointmentDate())
            .appointmentTime(a.getAppointmentTime())
            .status(a.getStatus().name())
            .createdAt(a.getCreatedAt())
            .build();
    }
}
