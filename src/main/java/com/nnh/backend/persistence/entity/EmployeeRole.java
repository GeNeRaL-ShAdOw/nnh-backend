package com.nnh.backend.persistence.entity;

public enum EmployeeRole {
    /** Can view and edit appointments. Every change requires ID + password confirmation. */
    CARE_STAFF,

    /** Full access: appointments, employee management, doctor absences.
     *  Changes require password + typing "CONFIRM". */
    ADMIN
}
