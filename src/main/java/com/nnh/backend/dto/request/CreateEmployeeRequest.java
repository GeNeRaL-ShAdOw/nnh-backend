package com.nnh.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEmployeeRequest {

    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    /** Optional on updates — omit or leave blank to keep the existing password. */
    private String password;

    @NotBlank
    private String role; // "CARE_STAFF" or "ADMIN"
}
