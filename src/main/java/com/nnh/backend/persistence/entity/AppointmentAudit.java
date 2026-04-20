package com.nnh.backend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long appointmentId;

    /** Display name of the employee who made the change. */
    @Column(nullable = false)
    private String changedByName;

    @Column(nullable = false)
    private String changedByEmail;

    @Column(nullable = false)
    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    @CreationTimestamp
    private LocalDateTime changedAt;
}
