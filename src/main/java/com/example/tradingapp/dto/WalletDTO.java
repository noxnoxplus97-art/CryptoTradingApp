package com.example.tradingapp.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Long id;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
}
