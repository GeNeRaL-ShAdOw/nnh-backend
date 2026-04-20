package com.nnh.backend.controller;

import com.nnh.backend.dto.request.ContactRequest;
import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.ContactResponse;
import com.nnh.backend.orchestration.ContactOrchestrator;
import com.nnh.backend.persistence.entity.ContactStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contact message endpoints.
 *
 *  POST   /api/contact              – submit a contact form
 *  GET    /api/contact              – list all messages (admin)
 *  GET    /api/contact/{id}         – fetch single message (admin)
 *  PATCH  /api/contact/{id}/status  – mark as READ / RESPONDED (admin)
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactOrchestrator orchestrator;

    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> submit(
        @Valid @RequestBody ContactRequest request) {
        ContactResponse response = orchestrator.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Message received. We will get back to you within 24 hours.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orchestrator.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(orchestrator.getAll()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ContactResponse>> updateStatus(
        @PathVariable Long id,
        @RequestParam ContactStatus status) {
        return ResponseEntity.ok(
            ApiResponse.success("Status updated to " + status, orchestrator.updateStatus(id, status)));
    }
}
