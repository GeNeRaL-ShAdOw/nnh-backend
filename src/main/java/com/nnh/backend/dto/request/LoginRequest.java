package com.nnh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /**
     * Accepts either an email address (e.g. care@nandanursinghome.in)
     * or an employee ID (e.g. NNHE-1234567).
     */
    @NotBlank
    private String loginId;

    @NotBlank
    private String password;
}
