package com.nnh.backend.persistence.entity;

/**
 * Lifecycle states for an appointment.
 *
 *  PENDING         – submitted by patient, awaiting staff review
 *  IN_CONSULTATION – patient is present and being seen
 *  CONFIRMED       – set automatically when a bill is generated; not manually selectable
 *  COMPLETED       – appointment has taken place
 *  CANCELLED       – cancelled by patient or clinic
 */
public enum AppointmentStatus {
    PENDING,
    IN_CONSULTATION,
    CONFIRMED,
    COMPLETED,
    CANCELLED
}
