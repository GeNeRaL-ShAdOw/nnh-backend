package com.nnh.backend.controller;

import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.MedicalServiceResponse;
import com.nnh.backend.persistence.entity.MedicalService;
import com.nnh.backend.service.MedicalServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Medical service catalogue endpoints (read-only for the public site).
 *
 *  GET  /api/services      – list all active services
 *  GET  /api/services/{id} – fetch a single service
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalServiceResponse>>> getAll() {
        List<MedicalServiceResponse> body = medicalServiceService.getActiveServices().stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(body));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalServiceResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(toResponse(medicalServiceService.getById(id))));
    }

    private MedicalServiceResponse toResponse(MedicalService s) {
        return MedicalServiceResponse.builder()
            .id(s.getId())
            .title(s.getTitle())
            .shortDesc(s.getShortDesc())
            .fullDesc(s.getFullDesc())
            .icon(s.getIcon())
            .color(s.getColor())
            .build();
    }
}
