package com.nnh.backend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalService {

    /** Slug identifier matching the frontend (e.g. "obstetrics"). */
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_desc")
    private String shortDesc;

    @Column(name = "full_desc", columnDefinition = "TEXT")
    private String fullDesc;

    /** Emoji icon used in the UI. */
    private String icon;

    /** Tailwind gradient string (e.g. "from-pink-400 to-rose-500"). */
    private String color;

    @Builder.Default
    private boolean active = true;
}
