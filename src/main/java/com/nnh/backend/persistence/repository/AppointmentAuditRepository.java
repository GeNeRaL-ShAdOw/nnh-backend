package com.nnh.backend.persistence.repository;

import com.nnh.backend.persistence.entity.AppointmentAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentAuditRepository extends JpaRepository<AppointmentAudit, Long> {

    List<AppointmentAudit> findByAppointmentIdOrderByChangedAtDesc(Long appointmentId);
}
