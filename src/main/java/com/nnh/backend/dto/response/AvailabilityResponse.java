package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Returned by GET /api/appointments/availability.
 *
 * The frontend uses this to:
 *  - Disable individual booked slots (bookedSlots list)
 *  - Block the entire date and show a banner (doctorAbsent + absenceMessage)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private List<String> bookedSlots;
    private boolean doctorAbsent;
    private String absenceMessage;
}
