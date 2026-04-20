package com.nnh.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LeaveResponse {
    private Long id;
    private String employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String status;
    private String reviewedByName;
    private LocalDateTime createdAt;
}
