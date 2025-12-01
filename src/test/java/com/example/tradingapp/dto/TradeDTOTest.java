package com.example.tradingapp.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TradeDTOTest {

    private TradeDTO trade;

    @BeforeEach
    void setUp() {
        trade = new TradeDTO();
    }

    @Test
    void testTradeConstructorAndGetters() {
        // Arrange & Act
        trade.setId(1L);
        trade.setSymbol("ETHUSDT");
        trade.setType("BUY");
        trade.setQuantity(new BigDecimal("2.5"));
        trade.setPrice(new BigDecimal("3000"));
        trade.setTotalAmount(new BigDecimal("7500"));
        trade.setStatus("COMPLETED");
        LocalDateTime now = LocalDateTime.now();
        trade.setTimestamp(now);

        // Assert
        assertEquals(1L, trade.getId());
        assertEquals("ETHUSDT", trade.getSymbol());
        assertEquals("BUY", trade.getType());
        assertEquals(new BigDecimal("2.5"), trade.getQuantity());
        assertEquals(new BigDecimal("3000"), trade.getPrice());
        assertEquals(new BigDecimal("7500"), trade.getTotalAmount());
        assertEquals("COMPLETED", trade.getStatus());
        assertEquals(now, trade.getTimestamp());
    }

    @Test
    void testTradeBuyType() {
        // Arrange & Act
        trade.setType("BUY");

        // Assert
        assertEquals("BUY", trade.getType());
    }

    @Test
    void testTradeSellType() {
        // Arrange & Act
        trade.setType("SELL");

        // Assert
        assertEquals("SELL", trade.getType());
    }

    @Test
    void testTradeWithLargeQuantity() {
        // Arrange & Act
        trade.setQuantity(new BigDecimal("1000000.12345678"));

        // Assert
        assertEquals(new BigDecimal("1000000.12345678"), trade.getQuantity());
    }

    @Test
    void testTradeWithZeroQuantity() {
        // Arrange & Act
        trade.setQuantity(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO, trade.getQuantity());
    }

    @Test
    void testTradeWithNegativePrice() {
        // Arrange & Act
        trade.setPrice(new BigDecimal("-100"));

        // Assert
        assertEquals(new BigDecimal("-100"), trade.getPrice());
    }

    @Test
    void testTradeEquality() {
        // Arrange
        TradeDTO trade1 = new TradeDTO();
        trade1.setId(1L);
        trade1.setSymbol("BTCUSDT");

        TradeDTO trade2 = new TradeDTO();
        trade2.setId(1L);
        trade2.setSymbol("BTCUSDT");

        // Act & Assert
        assertEquals(trade1.getId(), trade2.getId());
        assertEquals(trade1.getSymbol(), trade2.getSymbol());
    }

    @Test
    void testTradeWithNullValues() {
        // Arrange & Act
        trade.setId(null);
        trade.setSymbol(null);
        trade.setType(null);
        trade.setQuantity(null);
        trade.setPrice(null);
        trade.setTotalAmount(null);
        trade.setStatus(null);
        trade.setTimestamp(null);

        // Assert
        assertNull(trade.getId());
        assertNull(trade.getSymbol());
        assertNull(trade.getType());
        assertNull(trade.getQuantity());
        assertNull(trade.getPrice());
        assertNull(trade.getTotalAmount());
        assertNull(trade.getStatus());
        assertNull(trade.getTimestamp());
    }

    @Test
    void testTradeCalculateTotalAmount() {
        // Arrange
        trade.setQuantity(new BigDecimal("10"));
        trade.setPrice(new BigDecimal("250.50"));

        // Act
        BigDecimal total = trade.getQuantity().multiply(trade.getPrice());

        // Assert - compare using compareTo to avoid precision issues
        assertEquals(0, total.compareTo(new BigDecimal("2505.00")));
    }

    @Test
    void testTradeWithStatus() {
        // Arrange & Act
        trade.setStatus("PENDING");

        // Assert
        assertEquals("PENDING", trade.getStatus());
    }

    @Test
    void testTradeStatusChange() {
        // Arrange
        trade.setStatus("PENDING");

        // Act
        trade.setStatus("COMPLETED");

        // Assert
        assertEquals("COMPLETED", trade.getStatus());
    }
}
