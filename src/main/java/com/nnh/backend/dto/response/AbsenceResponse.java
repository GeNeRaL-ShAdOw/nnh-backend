package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceResponse {

    private Long id;
    private String doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String message;
    private String status;
    private String requestedByName;
    private String approvedByName;
    private LocalDateTime createdAt;
}
