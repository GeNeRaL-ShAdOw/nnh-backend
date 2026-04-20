package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {

    private String id;
    private String name;
    private String title;
    private String specialty;
    private String experience;
    private String imageUrl;
    private List<String> qualifications;
    private String availability;
}
