package com.nnh.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillItemResponse {
    private Long id;
    private String description;
    private String category;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
}
