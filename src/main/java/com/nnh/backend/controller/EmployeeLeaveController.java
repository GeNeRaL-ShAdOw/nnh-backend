package com.nnh.backend.controller;

import com.nnh.backend.dto.request.LeaveRequest;
import com.nnh.backend.dto.response.LeaveResponse;
import com.nnh.backend.service.EmployeeLeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class EmployeeLeaveController {

    private final EmployeeLeaveService leaveService;

    @GetMapping
    public ResponseEntity<?> getLeaves(
            @RequestParam(defaultValue = "false") boolean all,
            Principal principal) {
        List<LeaveResponse> leaves = all
                ? leaveService.getAll()
                : leaveService.getMyLeaves(principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "data", leaves));
    }

    @GetMapping("/approved")
    public ResponseEntity<?> getApproved() {
        return ResponseEntity.ok(Map.of("success", true, "data", leaveService.getApproved()));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody LeaveRequest req,
            Principal principal) {
        LeaveResponse resp = leaveService.create(req, principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "message", "Leave request submitted.", "data", resp));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            Principal principal) {
        LeaveResponse resp = leaveService.approve(id, principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "message", "Leave approved.", "data", resp));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            Principal principal) {
        LeaveResponse resp = leaveService.reject(id, principal.getName());
        return ResponseEntity.ok(Map.of("success", true, "message", "Leave rejected.", "data", resp));
    }
}
