package com.nnh.backend.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    /** Slug-style identifier matching the frontend (e.g. "dr-purohit"). */
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    /** Credentials line shown in the UI (e.g. "MBBS, DGO, DNB"). */
    private String title;

    private String specialty;
    private String experience;
    private String imageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "doctor_qualifications",
        joinColumns = @JoinColumn(name = "doctor_id")
    )
    @Column(name = "qualification")
    @Builder.Default
    private List<String> qualifications = new ArrayList<>();

    private String availability;

    @Builder.Default
    private boolean active = true;
}
