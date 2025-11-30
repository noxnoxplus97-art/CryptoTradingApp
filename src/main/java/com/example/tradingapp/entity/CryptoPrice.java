package com.example.tradingapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "crypto_prices", indexes = {
        @Index(name = "idx_symbol_timestamp", columnList = "symbol, timestamp DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CryptoPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal bidPrice;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal askPrice;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String source;

    @Column(precision = 18, scale = 8)
    private BigDecimal bidQty;

    @Column(precision = 18, scale = 8)
    private BigDecimal askQty;
}
