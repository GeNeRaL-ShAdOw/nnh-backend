package com.nnh.backend.controller;

import com.nnh.backend.dto.request.AppointmentRequest;
import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.AppointmentResponse;
import com.nnh.backend.dto.response.AuditEntryResponse;
import com.nnh.backend.dto.response.AvailabilityResponse;
import com.nnh.backend.orchestration.AppointmentOrchestrator;
import com.nnh.backend.persistence.entity.Appointment;
import com.nnh.backend.persistence.entity.AppointmentStatus;
import com.nnh.backend.persistence.entity.DoctorAbsence;
import com.nnh.backend.service.AppointmentService;
import com.nnh.backend.service.DoctorAbsenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentOrchestrator orchestrator;
    private final AppointmentService appointmentService;
    private final DoctorAbsenceService doctorAbsenceService;

    /** Public slot-availability check used by the booking form. */
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailability(
            @RequestParam String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Optional<DoctorAbsence> absence = doctorAbsenceService.findAbsence(doctorId, date);
        if (absence.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(AvailabilityResponse.builder()
                    .bookedSlots(List.of())
                    .doctorAbsent(true)
                    .absenceMessage(absence.get().getMessage())
                    .build()));
        }

        List<String> booked = appointmentService.getByDoctorAndDate(doctorId, date)
                .stream()
                .map(Appointment::getAppointmentTime)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(AvailabilityResponse.builder()
                .bookedSlots(booked)
                .doctorAbsent(false)
                .absenceMessage(null)
                .build()));
    }

    /** Public patient booking — creates with PENDING status. */
    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse response = orchestrator.book(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Appointment requested. We will confirm within 2 hours.", response));
    }

    /** Staff ad-hoc booking — creates with CONFIRMED status. Requires authentication. */
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAsStaff(
            @Valid @RequestBody AppointmentRequest request,
            Principal principal) {
        AppointmentResponse response = orchestrator.bookAsStaff(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Appointment confirmed.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orchestrator.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAll(
            @RequestParam(required = false) String email) {
        List<AppointmentResponse> result = email != null
            ? orchestrator.getByEmail(email)
            : orchestrator.getAll();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Status change — requires authentication; records audit trail. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success(
            "Status updated to " + status,
            orchestrator.updateStatus(id, status, principal.getName())));
    }

    /** Audit log for a single appointment — admin only (enforced in SecurityConfig). */
    @GetMapping("/{id}/audit")
    public ResponseEntity<ApiResponse<List<AuditEntryResponse>>> getAuditLog(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orchestrator.getAuditLog(id)));
    }
}
