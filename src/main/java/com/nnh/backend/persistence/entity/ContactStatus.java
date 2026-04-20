package com.nnh.backend.persistence.entity;

/**
 * Lifecycle states for a contact message.
 *
 *  NEW        – just received, not yet read
 *  READ       – seen by a staff member
 *  RESPONDED  – a staff member has sent a reply
 */
public enum ContactStatus {
    NEW,
    READ,
    RESPONDED
}
