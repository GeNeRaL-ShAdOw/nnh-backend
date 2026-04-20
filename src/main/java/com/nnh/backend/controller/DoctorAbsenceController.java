package com.nnh.backend.controller;

import com.nnh.backend.dto.request.AbsenceRequest;
import com.nnh.backend.dto.response.AbsenceResponse;
import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.service.DoctorAbsenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 *  GET    /api/absences/active      – public: approved current + future absences
 *  GET    /api/absences             – authenticated: all absence records
 *  POST   /api/absences             – authenticated: submit an absence request
 *  PATCH  /api/absences/{id}/approve – ADMIN only
 *  PATCH  /api/absences/{id}/reject  – ADMIN only
 *  DELETE /api/absences/{id}         – ADMIN only
 */
@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class DoctorAbsenceController {

    private final DoctorAbsenceService absenceService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AbsenceResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(absenceService.getUpcoming()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AbsenceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(absenceService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AbsenceResponse>> create(
            @Valid @RequestBody AbsenceRequest req,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Absence request submitted", absenceService.create(req, principal.getName())));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AbsenceResponse>> approve(
            @PathVariable Long id,
            Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Absence approved", absenceService.approve(id, principal.getName())));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AbsenceResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Absence rejected", absenceService.reject(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        absenceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Absence removed", null));
    }
}
