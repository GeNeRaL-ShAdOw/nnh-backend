package com.nnh.backend.controller;

import com.nnh.backend.dto.request.LoginRequest;
import com.nnh.backend.dto.request.VerifyPasswordRequest;
import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.LoginResponse;
import com.nnh.backend.exception.ResourceNotFoundException;
import com.nnh.backend.persistence.entity.Employee;
import com.nnh.backend.persistence.repository.EmployeeRepository;
import com.nnh.backend.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Login with either email OR employee ID (NNHE-xxxxxxx) + password.
     * The loginId field is checked: if it contains '@' it's treated as email,
     * otherwise as an employee ID.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        Employee employee = resolveEmployee(req.getLoginId());

        if (!employee.isActive()) {
            throw new BadCredentialsException("This account has been deactivated");
        }
        if (!passwordEncoder.matches(req.getPassword(), employee.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generate(employee.getEmail(), employee.getRole().name(), employee.getId());

        return ResponseEntity.ok(ApiResponse.success("Login successful", LoginResponse.builder()
                .token(token)
                .employeeId(employee.getId())
                .role(employee.getRole().name())
                .name(employee.getName())
                .email(employee.getEmail())
                .mustChangePassword(employee.isMustChangePassword())
                .build()));
    }

    /** Re-authentication gate called before every mutation. */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verify(
            @Valid @RequestBody VerifyPasswordRequest req,
            Principal principal) {

        Employee employee = employeeRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getId().equals(req.getEmployeeId())) {
            throw new BadCredentialsException("Employee ID does not match your account");
        }
        if (!passwordEncoder.matches(req.getPassword(), employee.getPasswordHash())) {
            throw new BadCredentialsException("Incorrect password");
        }

        return ResponseEntity.ok(ApiResponse.success("Verified", true));
    }

    /** Forced first-login password change. */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Principal principal) {

        Employee employee = employeeRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!passwordEncoder.matches(req.getCurrentPassword(), employee.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        if (req.getNewPassword().equals(req.getCurrentPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password");
        }

        employee.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        employee.setMustChangePassword(false);
        employeeRepository.save(employee);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Employee resolveEmployee(String loginId) {
        if (loginId.contains("@")) {
            return employeeRepository.findByEmail(loginId)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        }
        // Employee ID login (NNHE-xxxxxxx)
        return employeeRepository.findById(loginId)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    // ── Inner DTO ─────────────────────────────────────────────────────────────

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;

        @NotBlank
        @Size(min = 8, message = "New password must be at least 8 characters")
        private String newPassword;
    }
}
