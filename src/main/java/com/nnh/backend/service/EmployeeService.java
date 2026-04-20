package com.nnh.backend.service;

import com.nnh.backend.dto.request.CreateEmployeeRequest;
import com.nnh.backend.dto.response.EmployeeResponse;
import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.entity.EmployeeRole;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailScenarioService emailScenarioService;
    private final Random random = new Random();

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest req) {
        if (employeeRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An employee with email " + req.getEmail() + " already exists");
        }
        String id = generateEmployeeId();
        Employee employee = Employee.builder()
                .id(id)
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(EmployeeRole.valueOf(req.getRole()))
                .active(true)
                .mustChangePassword(true)
                .build();
        log.info("Creating employee {} ({}, {})", id, employee.getEmail(), employee.getRole());
        Employee saved = employeeRepository.save(employee);
        emailScenarioService.sendNewEmployeeWelcome(saved, req.getPassword());
        return toResponse(saved);
    }

    @Transactional
    public EmployeeResponse update(String id, CreateEmployeeRequest req) {
        Employee employee = findOrThrow(id);
        employee.setName(req.getName());
        employee.setRole(EmployeeRole.valueOf(req.getRole()));
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        log.info("Updating employee {}", id);
        return toResponse(employeeRepository.save(employee));
    }

    @Transactional
    public void deactivate(String id) {
        Employee employee = findOrThrow(id);
        guardProtected(employee);
        employee.setActive(false);
        employeeRepository.save(employee);
        log.info("Deactivated employee {}", id);
    }

    @Transactional
    public void permanentDelete(String id) {
        Employee employee = findOrThrow(id);
        guardProtected(employee);
        if (employee.isActive()) {
            throw new IllegalArgumentException("Employee must be deactivated before permanent deletion");
        }
        employeeRepository.delete(employee);
        log.info("Permanently deleted employee {}", id);
    }

    private void guardProtected(Employee employee) {
        if ("care@nandanursinghome.in".equals(employee.getEmail())) {
            throw new IllegalArgumentException("The care staff account is protected and cannot be deactivated or deleted");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Employee findOrThrow(String id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    /** Generates a unique NNHE-xxxxxxx ID, retrying up to 10 times on collision. */
    private String generateEmployeeId() {
        for (int attempt = 1; attempt <= 10; attempt++) {
            int digits = 1_000_000 + random.nextInt(9_000_000); // 7 digits: 1000000–9999999
            String id = "NNHE-" + digits;
            if (!employeeRepository.existsById(id)) {
                return id;
            }
            log.warn("Employee ID collision on attempt {}: {}", attempt, id);
        }
        throw new IllegalStateException("Could not generate a unique employee ID after 10 attempts");
    }

    private EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .role(e.getRole().name())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
