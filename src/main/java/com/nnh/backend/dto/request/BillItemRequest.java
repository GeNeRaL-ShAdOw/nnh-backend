package com.nnh.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillItemRequest {

    @NotBlank
    private String description;

    @NotBlank
    private String category;

    @NotNull
    @Positive
    private BigDecimal unitPrice;

    @Min(1)
    private int quantity;
}
