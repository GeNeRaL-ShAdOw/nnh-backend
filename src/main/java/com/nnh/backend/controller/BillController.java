package com.nnh.backend.controller;

import com.nnh.backend.dto.request.BillRequest;
import com.nnh.backend.dto.response.ApiResponse;
import com.nnh.backend.dto.response.BillResponse;
import com.nnh.backend.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> create(
            @Valid @RequestBody BillRequest request,
            Principal principal) {
        BillResponse response = billService.create(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Bill created.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(billService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(billService.getById(id)));
    }
}
