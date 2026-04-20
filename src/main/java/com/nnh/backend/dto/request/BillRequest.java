package com.nnh.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BillRequest {

    @NotBlank
    private String patientName;

    private String patientEmail;
    private String patientPhone;
    private Long appointmentId;

    @NotEmpty
    @Valid
    private List<BillItemRequest> items;
}
