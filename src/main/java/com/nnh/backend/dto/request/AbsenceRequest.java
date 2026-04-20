package com.nnh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AbsenceRequest {

    @NotBlank
    private String doctorId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}
