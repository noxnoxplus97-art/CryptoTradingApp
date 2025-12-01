package com.example.tradingapp.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponseDTO {
    private String symbol;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private String timestamp;
}
