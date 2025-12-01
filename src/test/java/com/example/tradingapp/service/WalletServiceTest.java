package com.example.tradingapp.service;

import com.example.tradingapp.dto.Wallet;
import com.example.tradingapp.entity.User;
import com.example.tradingapp.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private List<com.example.tradingapp.entity.Wallet> mockWallets;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        com.example.tradingapp.entity.Wallet usdtWallet = new com.example.tradingapp.entity.Wallet();
        usdtWallet.setId(1L);
        usdtWallet.setUser(testUser);
        usdtWallet.setCurrency("USDT");
        usdtWallet.setBalance(new BigDecimal("50000"));
        usdtWallet.setAvailableBalance(new BigDecimal("50000"));

        com.example.tradingapp.entity.Wallet ethWallet = new com.example.tradingapp.entity.Wallet();
        ethWallet.setId(2L);
        ethWallet.setUser(testUser);
        ethWallet.setCurrency("ETH");
        ethWallet.setBalance(new BigDecimal("1.5"));
        ethWallet.setAvailableBalance(new BigDecimal("1.5"));

        mockWallets = List.of(usdtWallet, ethWallet);
    }

    @Test
    void testGetUserWallets_Success() {
        // Arrange
        when(walletRepository.findByUser(testUser))
                .thenReturn(mockWallets);

        // Act
        List<Wallet> wallets = walletService.getUserWallets(testUser);

        // Assert
        assertNotNull(wallets);
        assertEquals(2, wallets.size());
        assertEquals("USDT", wallets.get(0).getCurrency());
        assertEquals("ETH", wallets.get(1).getCurrency());

        verify(walletRepository).findByUser(testUser);
    }

    @Test
    void testGetUserWallets_NoWallets() {
        // Arrange
        when(walletRepository.findByUser(testUser))
                .thenReturn(List.of());

        // Act
        List<Wallet> wallets = walletService.getUserWallets(testUser);

        // Assert
        assertNotNull(wallets);
        assertEquals(0, wallets.size());

        verify(walletRepository).findByUser(testUser);
    }

    @Test
    void testGetWalletByCurrency_Success() {
        // Arrange
        com.example.tradingapp.entity.Wallet usdtWallet = new com.example.tradingapp.entity.Wallet();
        usdtWallet.setId(1L);
        usdtWallet.setUser(testUser);
        usdtWallet.setCurrency("USDT");
        usdtWallet.setBalance(new BigDecimal("50000"));
        usdtWallet.setAvailableBalance(new BigDecimal("50000"));

        when(walletRepository.findByUserAndCurrency(testUser, "USDT"))
                .thenReturn(Optional.of(usdtWallet));

        // Act
        Wallet wallet = walletService.getWalletByCurrency(testUser, "USDT");

        // Assert
        assertNotNull(wallet);
        assertEquals("USDT", wallet.getCurrency());
        assertEquals(new BigDecimal("50000"), wallet.getBalance());

        verify(walletRepository).findByUserAndCurrency(testUser, "USDT");
    }

    @Test
    void testGetWalletByCurrency_NotFound() {
        // Arrange
        when(walletRepository.findByUserAndCurrency(testUser, "XYZ"))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            walletService.getWalletByCurrency(testUser, "XYZ");
        });
        assertEquals("Wallet not found for currency: XYZ", exception.getMessage());

        verify(walletRepository).findByUserAndCurrency(testUser, "XYZ");
    }

    @Test
    void testGetWalletByCurrency_ValidateMapping() {
        // Arrange
        com.example.tradingapp.entity.Wallet ethWallet = new com.example.tradingapp.entity.Wallet();
        ethWallet.setId(2L);
        ethWallet.setUser(testUser);
        ethWallet.setCurrency("ETH");
        ethWallet.setBalance(new BigDecimal("2.5"));
        ethWallet.setAvailableBalance(new BigDecimal("1.8"));

        when(walletRepository.findByUserAndCurrency(testUser, "ETH"))
                .thenReturn(Optional.of(ethWallet));

        // Act
        Wallet wallet = walletService.getWalletByCurrency(testUser, "ETH");

        // Assert
        assertNotNull(wallet);
        assertEquals(2L, wallet.getId());
        assertEquals("ETH", wallet.getCurrency());
        assertEquals(new BigDecimal("2.5"), wallet.getBalance());
        assertEquals(new BigDecimal("1.8"), wallet.getAvailableBalance());
    }
}
