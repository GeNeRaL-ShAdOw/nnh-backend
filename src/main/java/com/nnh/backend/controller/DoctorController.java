package com.nnh.backend.controller;

import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.DoctorResponse;
import com.nnh.backend.persistence.entity.Doctor;
import com.nnh.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Doctor catalogue endpoints (read-only for the public site).
 *
 *  GET  /api/doctors      – list all active doctors
 *  GET  /api/doctors/{id} – fetch a single doctor
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAll() {
        List<DoctorResponse> body = doctorService.getActiveDoctors().stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(toResponse(doctorService.getById(id))));
    }

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
            .id(d.getId())
            .name(d.getName())
            .title(d.getTitle())
            .specialty(d.getSpecialty())
            .experience(d.getExperience())
            .imageUrl(d.getImageUrl())
            .qualifications(d.getQualifications())
            .availability(d.getAvailability())
            .build();
    }
}
