package com.example.tradingapp.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestDTO {
    private String symbol;
    private String type;
    private BigDecimal quantity;
}
