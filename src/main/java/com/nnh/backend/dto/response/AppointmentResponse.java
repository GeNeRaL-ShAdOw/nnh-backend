package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Boolean newPatient;
    private String doctorId;
    private String doctorName;
    private String serviceId;
    private String serviceName;
    private String reason;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String status;
    private LocalDateTime createdAt;
}
