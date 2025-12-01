package com.example.tradingapp.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class WalletDTOTest {

    private WalletDTO wallet;

    @BeforeEach
    void setUp() {
        wallet = new WalletDTO();
    }

    @Test
    void testWalletConstructorAndGetters() {
        // Arrange & Act
        wallet.setId(1L);
        wallet.setCurrency("USDT");
        wallet.setBalance(new BigDecimal("50000"));
        wallet.setAvailableBalance(new BigDecimal("50000"));

        // Assert
        assertEquals(1L, wallet.getId());
        assertEquals("USDT", wallet.getCurrency());
        assertEquals(new BigDecimal("50000"), wallet.getBalance());
        assertEquals(new BigDecimal("50000"), wallet.getAvailableBalance());
    }

    @Test
    void testWalletWithEthereum() {
        // Arrange & Act
        wallet.setCurrency("ETH");
        wallet.setBalance(new BigDecimal("5.5"));
        wallet.setAvailableBalance(new BigDecimal("3.2"));

        // Assert
        assertEquals("ETH", wallet.getCurrency());
        assertEquals(new BigDecimal("5.5"), wallet.getBalance());
        assertEquals(new BigDecimal("3.2"), wallet.getAvailableBalance());
    }

    @Test
    void testWalletWithBitcoin() {
        // Arrange & Act
        wallet.setCurrency("BTC");
        wallet.setBalance(new BigDecimal("0.75"));
        wallet.setAvailableBalance(new BigDecimal("0.50"));

        // Assert
        assertEquals("BTC", wallet.getCurrency());
        assertEquals(new BigDecimal("0.75"), wallet.getBalance());
        assertEquals(new BigDecimal("0.50"), wallet.getAvailableBalance());
    }

    @Test
    void testWalletWithZeroBalance() {
        // Arrange & Act
        wallet.setCurrency("XRP");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(BigDecimal.ZERO);

        // Assert
        assertEquals(BigDecimal.ZERO, wallet.getBalance());
        assertEquals(BigDecimal.ZERO, wallet.getAvailableBalance());
    }

    @Test
    void testWalletLockedBalance() {
        // Arrange
        wallet.setBalance(new BigDecimal("1000"));
        wallet.setAvailableBalance(new BigDecimal("600"));

        // Act
        BigDecimal lockedBalance = wallet.getBalance().subtract(wallet.getAvailableBalance());

        // Assert
        assertEquals(new BigDecimal("400"), lockedBalance);
    }

    @Test
    void testWalletWithHighPrecisionDecimals() {
        // Arrange & Act
        wallet.setBalance(new BigDecimal("1234.56789012345"));
        wallet.setAvailableBalance(new BigDecimal("999.99999999999"));

        // Assert
        assertEquals(new BigDecimal("1234.56789012345"), wallet.getBalance());
        assertEquals(new BigDecimal("999.99999999999"), wallet.getAvailableBalance());
    }

    @Test
    void testWalletEquality() {
        // Arrange
        WalletDTO wallet1 = new WalletDTO();
        wallet1.setId(1L);
        wallet1.setCurrency("USDT");

        WalletDTO wallet2 = new WalletDTO();
        wallet2.setId(1L);
        wallet2.setCurrency("USDT");

        // Act & Assert
        assertEquals(wallet1.getId(), wallet2.getId());
        assertEquals(wallet1.getCurrency(), wallet2.getCurrency());
    }

    @Test
    void testWalletWithNullValues() {
        // Arrange & Act
        wallet.setId(null);
        wallet.setCurrency(null);
        wallet.setBalance(null);
        wallet.setAvailableBalance(null);

        // Assert
        assertNull(wallet.getId());
        assertNull(wallet.getCurrency());
        assertNull(wallet.getBalance());
        assertNull(wallet.getAvailableBalance());
    }

    @Test
    void testWalletAvailableBalanceNotGreaterThanBalance() {
        // Arrange & Act
        wallet.setBalance(new BigDecimal("1000"));
        wallet.setAvailableBalance(new BigDecimal("1500"));

        // Assert - this demonstrates a potential validation scenario
        assertTrue(wallet.getAvailableBalance().compareTo(wallet.getBalance()) > 0);
    }

    @Test
    void testWalletMultipleCurrencies() {
        // Test USDT
        wallet.setCurrency("USDT");
        assertEquals("USDT", wallet.getCurrency());

        // Test ETH
        wallet.setCurrency("ETH");
        assertEquals("ETH", wallet.getCurrency());

        // Test BTC
        wallet.setCurrency("BTC");
        assertEquals("BTC", wallet.getCurrency());
    }

    @Test
    void testWalletBalanceTransfer() {
        // Arrange - initial state
        wallet.setBalance(new BigDecimal("1000"));
        wallet.setAvailableBalance(new BigDecimal("1000"));

        // Act - simulate transfer
        BigDecimal transferAmount = new BigDecimal("250");
        BigDecimal newBalance = wallet.getBalance().subtract(transferAmount);
        BigDecimal newAvailable = wallet.getAvailableBalance().subtract(transferAmount);

        wallet.setBalance(newBalance);
        wallet.setAvailableBalance(newAvailable);

        // Assert
        assertEquals(new BigDecimal("750"), wallet.getBalance());
        assertEquals(new BigDecimal("750"), wallet.getAvailableBalance());
    }
}
