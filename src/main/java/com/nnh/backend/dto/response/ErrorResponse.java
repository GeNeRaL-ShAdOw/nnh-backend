package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Envelope for all error responses.
 *
 * <pre>
 * {
 *   "timestamp": "2025-04-19T10:30:00",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "fieldErrors": { "email": "Invalid email address" }
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;

    /** Populated only for validation (400) errors. */
    private Map<String, String> fieldErrors;

    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(httpStatus.value())
            .error(httpStatus.getReasonPhrase())
            .message(message)
            .build();
    }
}
