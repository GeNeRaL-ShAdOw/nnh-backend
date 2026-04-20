package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntryResponse {

    private Long id;
    private String changedByName;
    private String changedByEmail;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime changedAt;
}
