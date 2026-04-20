package com.nnh.backend.service;

import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.Doctor;
import com.nnh.backend.persistence.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Domain logic for doctor records.
 * Read-only by default; mutations go through admin flows (not yet exposed).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public List<Doctor> getActiveDoctors() {
        log.debug("Fetching all active doctors");
        return doctorRepository.findByActiveTrue();
    }

    /** Throws {@link ResourceNotFoundException} if the doctor is missing or inactive. */
    public Doctor getById(String id) {
        log.debug("Fetching doctor with id '{}'", id);
        return doctorRepository.findById(id)
            .filter(Doctor::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }
}
