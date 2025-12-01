package com.example.tradingapp.config;

import com.example.tradingapp.entity.User;
import com.example.tradingapp.entity.Wallet;
import com.example.tradingapp.repository.UserRepository;
import com.example.tradingapp.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@trading.com");
    }

    @Test
    void testInitializeDefaultUser_UserDoesNotExist_CreatesUserAndWallets() throws Exception {
        // Arrange - user doesn't exist
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.findByUserAndCurrency(testUser, "USDT")).thenReturn(Optional.empty());
        when(walletRepository.findByUserAndCurrency(testUser, "ETH")).thenReturn(Optional.empty());
        when(walletRepository.findByUserAndCurrency(testUser, "BTC")).thenReturn(Optional.empty());

        // Act
        dataInitializer.run();

        // Assert - user created
        verify(userRepository).save(any(User.class));
        
        // Assert - all 3 wallets created with correct balances
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(3)).save(walletCaptor.capture());

        var savedWallets = walletCaptor.getAllValues();
        assertEquals(3, savedWallets.size());

        // Verify USDT wallet
        Wallet usdtWallet = savedWallets.stream()
                .filter(w -> w.getCurrency().equals("USDT"))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("50000"), usdtWallet.getBalance());
        assertEquals(new BigDecimal("50000"), usdtWallet.getAvailableBalance());

        // Verify ETH wallet
        Wallet ethWallet = savedWallets.stream()
                .filter(w -> w.getCurrency().equals("ETH"))
                .findFirst()
                .orElseThrow();
        assertEquals(BigDecimal.ZERO, ethWallet.getBalance());
        assertEquals(BigDecimal.ZERO, ethWallet.getAvailableBalance());

        // Verify BTC wallet
        Wallet btcWallet = savedWallets.stream()
                .filter(w -> w.getCurrency().equals("BTC"))
                .findFirst()
                .orElseThrow();
        assertEquals(BigDecimal.ZERO, btcWallet.getBalance());
        assertEquals(BigDecimal.ZERO, btcWallet.getAvailableBalance());
    }

    @Test
    void testInitializeDefaultUser_UserExists_SkipsCreation() throws Exception {
        // Arrange - user already exists
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        dataInitializer.run();

        // Assert - user save not called
        verify(userRepository, never()).save(any(User.class));
        
        // Assert - no wallets created
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testInitializeDefaultUser_UserExistsWithExistingWallets_SkipsWalletCreation() throws Exception {
        // Arrange - user exists with wallets
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Wallets already exist with different balances
        Wallet existingUsdtWallet = new Wallet();
        existingUsdtWallet.setCurrency("USDT");
        existingUsdtWallet.setBalance(new BigDecimal("25000")); // Different balance
        existingUsdtWallet.setAvailableBalance(new BigDecimal("25000"));

        Wallet existingEthWallet = new Wallet();
        existingEthWallet.setCurrency("ETH");
        existingEthWallet.setBalance(new BigDecimal("5"));
        existingEthWallet.setAvailableBalance(new BigDecimal("3.5"));

        when(walletRepository.findByUserAndCurrency(testUser, "USDT")).thenReturn(Optional.of(existingUsdtWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH")).thenReturn(Optional.of(existingEthWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "BTC")).thenReturn(Optional.empty());

        // Act
        dataInitializer.run();

        // Assert - user created but wallets NOT updated
        verify(userRepository).save(any(User.class));
        
        // Only BTC wallet should be created (the one that doesn't exist)
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(1)).save(walletCaptor.capture());

        Wallet createdWallet = walletCaptor.getValue();
        assertEquals("BTC", createdWallet.getCurrency());
        assertEquals(BigDecimal.ZERO, createdWallet.getBalance());
    }

    @Test
    void testInitializeDefaultUser_PreservesExistingBalances() throws Exception {
        // Arrange - user exists with wallets that have been traded
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        Wallet tradedUsdtWallet = new Wallet();
        tradedUsdtWallet.setCurrency("USDT");
        tradedUsdtWallet.setBalance(new BigDecimal("42500")); // User traded 7500
        tradedUsdtWallet.setAvailableBalance(new BigDecimal("42500"));

        Wallet tradedEthWallet = new Wallet();
        tradedEthWallet.setCurrency("ETH");
        tradedEthWallet.setBalance(new BigDecimal("2.75")); // User bought 2.75 ETH
        tradedEthWallet.setAvailableBalance(new BigDecimal("2.75"));

        when(walletRepository.findByUserAndCurrency(testUser, "USDT")).thenReturn(Optional.of(tradedUsdtWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH")).thenReturn(Optional.of(tradedEthWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "BTC")).thenReturn(Optional.empty());

        // Act
        dataInitializer.run();

        // Assert - existing balances are not modified or recreated
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(1)).save(walletCaptor.capture()); // Only BTC created

        Wallet createdWallet = walletCaptor.getValue();
        assertEquals("BTC", createdWallet.getCurrency());
        assertEquals(BigDecimal.ZERO, createdWallet.getBalance());

        // Verify balances remain unchanged (wallets not recreated)
        assertEquals(new BigDecimal("42500"), tradedUsdtWallet.getBalance());
        assertEquals(new BigDecimal("2.75"), tradedEthWallet.getBalance());
    }

    @Test
    void testInitializeWalletIfNotExists_WalletExists_DoesNotCreate() throws Exception {
        // Arrange
        Wallet existingWallet = new Wallet();
        existingWallet.setCurrency("USDT");
        existingWallet.setBalance(new BigDecimal("30000"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.findByUserAndCurrency(testUser, "USDT")).thenReturn(Optional.of(existingWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH")).thenReturn(Optional.empty());
        when(walletRepository.findByUserAndCurrency(testUser, "BTC")).thenReturn(Optional.empty());

        // Act
        dataInitializer.run();

        // Assert - wallet not recreated, only new ones created
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(2)).save(walletCaptor.capture()); // Only ETH and BTC

        var currencies = walletCaptor.getAllValues().stream()
                .map(Wallet::getCurrency)
                .toList();
        
        assertTrue(currencies.contains("ETH"));
        assertTrue(currencies.contains("BTC"));
        assertFalse(currencies.contains("USDT"));
    }

    @Test
    void testInitializeWalletIfNotExists_NewWallet_CreatesWithCorrectBalance() throws Exception {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.findByUserAndCurrency(testUser, "USDT")).thenReturn(Optional.empty());
        when(walletRepository.findByUserAndCurrency(testUser, "ETH")).thenReturn(Optional.empty());
        when(walletRepository.findByUserAndCurrency(testUser, "BTC")).thenReturn(Optional.empty());

        // Act
        dataInitializer.run();

        // Assert
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(3)).save(walletCaptor.capture());

        // Verify each wallet has correct balance and user
        walletCaptor.getAllValues().forEach(wallet -> {
            assertEquals(testUser, wallet.getUser());
            assertEquals(wallet.getBalance(), wallet.getAvailableBalance());
        });
    }
}
