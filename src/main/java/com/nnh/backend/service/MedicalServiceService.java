package com.nnh.backend.service;

import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.MedicalService;
import com.nnh.backend.persistence.repository.MedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Domain logic for medical service catalogue entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicalServiceService {

    private final MedicalServiceRepository serviceRepository;

    public List<MedicalService> getActiveServices() {
        log.debug("Fetching all active medical services");
        return serviceRepository.findByActiveTrue();
    }

    /** Throws {@link ResourceNotFoundException} if the service is missing or inactive. */
    public MedicalService getById(String id) {
        log.debug("Fetching medical service with id '{}'", id);
        return serviceRepository.findById(id)
            .filter(MedicalService::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Medical service not found: " + id));
    }
}
