package com.nnh.backend.controller;

import com.nnh.backend.dto.request.CreateEmployeeRequest;
import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.EmployeeResponse;
import com.nnh.backend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Employee management — ADMIN role only (enforced in SecurityConfig).
 *
 *  GET    /api/employees              – list all employees
 *  POST   /api/employees              – create a new employee (ID auto-generated as NNHE-xxxxxxx)
 *  PUT    /api/employees/{id}         – update name / role / password
 *  DELETE /api/employees/{id}         – deactivate (soft-delete)
 *  DELETE /api/employees/{id}/permanent – permanently remove a deactivated employee
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created", employeeService.create(req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated", employeeService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String id) {
        employeeService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", null));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentDelete(@PathVariable String id) {
        employeeService.permanentDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Employee permanently deleted", null));
    }
}
