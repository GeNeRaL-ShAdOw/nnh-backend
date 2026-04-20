package com.nnh.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BillResponse {
    private Long id;
    private String patientName;
    private String patientEmail;
    private String patientPhone;
    private Long appointmentId;
    private String createdById;
    private String createdByName;
    private BigDecimal total;
    private List<BillItemResponse> items;
    private LocalDateTime createdAt;
}
