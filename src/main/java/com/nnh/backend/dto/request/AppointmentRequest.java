package com.nnh.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentRequest {

    // ── Personal info ─────────────────────────────────────────────────────────
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]{7,15}$", message = "Invalid phone number")
    private String phone;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Please indicate whether this is a new patient visit")
    private Boolean newPatient;

    // ── Medical details ───────────────────────────────────────────────────────
    @NotBlank(message = "Doctor selection is required")
    private String doctorId;

    @NotBlank(message = "Service selection is required")
    private String serviceId;

    /** Optional freeform symptoms / reason for visit. */
    private String reason;

    // ── Scheduling ────────────────────────────────────────────────────────────
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be a future date")
    private LocalDate appointmentDate;

    @NotBlank(message = "Appointment time is required")
    private String appointmentTime;
}
