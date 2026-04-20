package com.nnh.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalServiceResponse {

    private String id;
    private String title;
    private String shortDesc;
    private String fullDesc;
    private String icon;
    private String color;
}
