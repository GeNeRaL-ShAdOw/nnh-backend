package com.nnh.backend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAbsence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Matches Doctor.id (e.g. "dr-purohit"). */
    @Column(nullable = false)
    private String doctorId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    /** Human-readable message shown on the booking page, e.g.
     *  "Dr. Vaishnavi is not present from May 1, 2025 to May 7, 2025" */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AbsenceStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private Employee approvedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
