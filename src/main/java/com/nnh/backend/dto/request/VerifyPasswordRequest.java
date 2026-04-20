package com.nnh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPasswordRequest {

    /** NNHE-xxxxxxx ID of the logged-in employee. Must match the authenticated user. */
    @NotBlank
    private String employeeId;

    @NotBlank
    private String password;
}
