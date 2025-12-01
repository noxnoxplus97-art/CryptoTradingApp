package com.example.tradingapp.service;

import com.example.tradingapp.dto.TradeDTO;
import com.example.tradingapp.dto.WalletDTO;
import com.example.tradingapp.entity.CryptoPrice;
import com.example.tradingapp.entity.User;
import com.example.tradingapp.entity.Wallet;
import com.example.tradingapp.repository.CryptoPriceRepository;
import com.example.tradingapp.repository.TradeRepository;
import com.example.tradingapp.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @InjectMocks
    private TradeService tradeService;

        private User testUser;
        private Wallet usdtWallet;
        private Wallet ethWallet;
        private CryptoPrice ethPrice;

    @BeforeEach
        void setUp() {
                // Create test user
                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");

                // Create USDT wallet
                usdtWallet = new Wallet();
                usdtWallet.setId(1L);
                usdtWallet.setUser(testUser);
                usdtWallet.setCurrency("USDT");
                usdtWallet.setBalance(new BigDecimal("50000"));
                usdtWallet.setAvailableBalance(new BigDecimal("50000"));

                // Create ETH wallet
                ethWallet = new Wallet();
                ethWallet.setId(2L);
                ethWallet.setUser(testUser);
                ethWallet.setCurrency("ETH");
                ethWallet.setBalance(BigDecimal.ZERO);
                ethWallet.setAvailableBalance(BigDecimal.ZERO);

                // Create ETH price data
                ethPrice = new CryptoPrice();
                ethPrice.setId(1L);
                ethPrice.setSymbol("ETHUSDT");
                ethPrice.setAskPrice(new BigDecimal("3000"));
                ethPrice.setBidPrice(new BigDecimal("2999"));
                ethPrice.setTimestamp(LocalDateTime.now());
        }

    @Test
    void testExecuteBuyTrade_Success() {
        // Arrange
        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.of(ethPrice));
        when(walletRepository.findByUserAndCurrency(testUser, "USDT"))
                .thenReturn(Optional.of(usdtWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH"))
                .thenReturn(Optional.of(ethWallet));
        when(tradeRepository.save(any(com.example.tradingapp.entity.Trade.class)))
                .thenAnswer(invocation -> {
                    com.example.tradingapp.entity.Trade trade = invocation.getArgument(0);
                    trade.setId(1L);
                    return trade;
                });

        // Act
        TradeDTO trade = tradeService.executeTrade(testUser, "ETHUSDT", "BUY", new BigDecimal("1"));

        // Assert
        assertNotNull(trade);
        assertEquals("ETHUSDT", trade.getSymbol());
        assertEquals("BUY", trade.getType());
        assertEquals(new BigDecimal("1"), trade.getQuantity());
        assertEquals(new BigDecimal("3000"), trade.getPrice());

        // Verify repository calls
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(tradeRepository).save(any(com.example.tradingapp.entity.Trade.class));
    }

    @Test
    void testExecuteBuyTrade_InsufficientUSDT() {
        // Arrange - wallet with insufficient balance
        Wallet lowUSDT = new Wallet();
        lowUSDT.setId(1L);
        lowUSDT.setUser(testUser);
        lowUSDT.setCurrency("USDT");
        lowUSDT.setBalance(new BigDecimal("500"));
        lowUSDT.setAvailableBalance(new BigDecimal("500"));

        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.of(ethPrice));
        when(walletRepository.findByUserAndCurrency(testUser, "USDT"))
                .thenReturn(Optional.of(lowUSDT));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.executeTrade(testUser, "ETHUSDT", "BUY", new BigDecimal("1"));
        });
        assertEquals("Insufficient USDT balance", exception.getMessage());
    }

    @Test
    void testExecuteBuyTrade_InvalidSymbol() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.executeTrade(testUser, "INVALID", "BUY", new BigDecimal("1"));
        });
        assertEquals("Invalid trading symbol: INVALID", exception.getMessage());
    }

    @Test
    void testExecuteBuyTrade_NoPriceData() {
        // Arrange
        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.executeTrade(testUser, "ETHUSDT", "BUY", new BigDecimal("1"));
        });
        assertEquals("No price data available for symbol: ETHUSDT", exception.getMessage());
    }

    @Test
    void testExecuteSellTrade_Success() {
        // Arrange - wallet with ETH
        Wallet ethWithBalance = new Wallet();
        ethWithBalance.setId(2L);
        ethWithBalance.setUser(testUser);
        ethWithBalance.setCurrency("ETH");
        ethWithBalance.setBalance(new BigDecimal("2"));
        ethWithBalance.setAvailableBalance(new BigDecimal("2"));

        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.of(ethPrice));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH"))
                .thenReturn(Optional.of(ethWithBalance));
        when(walletRepository.findByUserAndCurrency(testUser, "USDT"))
                .thenReturn(Optional.of(usdtWallet));
        when(tradeRepository.save(any(com.example.tradingapp.entity.Trade.class)))
                .thenAnswer(invocation -> {
                    com.example.tradingapp.entity.Trade trade = invocation.getArgument(0);
                    trade.setId(1L);
                    return trade;
                });

        // Act
        TradeDTO trade = tradeService.executeTrade(testUser, "ETHUSDT", "SELL", new BigDecimal("1"));

        // Assert
        assertNotNull(trade);
        assertEquals("ETHUSDT", trade.getSymbol());
        assertEquals("SELL", trade.getType());
        assertEquals(new BigDecimal("1"), trade.getQuantity());
        assertEquals(new BigDecimal("2999"), trade.getPrice()); // Bid price

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(tradeRepository).save(any(com.example.tradingapp.entity.Trade.class));
    }

    @Test
    void testExecuteSellTrade_InsufficientCrypto() {
        // Arrange - wallet with insufficient ETH
        Wallet lowEth = new Wallet();
        lowEth.setId(2L);
        lowEth.setUser(testUser);
        lowEth.setCurrency("ETH");
        lowEth.setBalance(new BigDecimal("0.5"));
        lowEth.setAvailableBalance(new BigDecimal("0.5"));

        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.of(ethPrice));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH"))
                .thenReturn(Optional.of(lowEth));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            tradeService.executeTrade(testUser, "ETHUSDT", "SELL", new BigDecimal("1"));
        });
        assertEquals("Insufficient ETH balance", exception.getMessage());
    }

    @Test
    void testBuyTradeUpdatesUSDTBalance() {
        // Arrange
        BigDecimal quantity = new BigDecimal("2");
        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.of(ethPrice));
        when(walletRepository.findByUserAndCurrency(testUser, "USDT"))
                .thenReturn(Optional.of(usdtWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH"))
                .thenReturn(Optional.of(ethWallet));
        when(tradeRepository.save(any(com.example.tradingapp.entity.Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal expectedUSDTBalance = new BigDecimal("50000").subtract(quantity.multiply(new BigDecimal("3000")));

        // Act
        tradeService.executeTrade(testUser, "ETHUSDT", "BUY", quantity);

        // Assert
        assertEquals(expectedUSDTBalance, usdtWallet.getAvailableBalance());
    }

    @Test
    void testBuyTradeUpdatesETHBalance() {
        // Arrange
        BigDecimal quantity = new BigDecimal("2");
        when(cryptoPriceRepository.findLatestBySymbol("ETHUSDT"))
                .thenReturn(Optional.of(ethPrice));
        when(walletRepository.findByUserAndCurrency(testUser, "USDT"))
                .thenReturn(Optional.of(usdtWallet));
        when(walletRepository.findByUserAndCurrency(testUser, "ETH"))
                .thenReturn(Optional.of(ethWallet));
        when(tradeRepository.save(any(com.example.tradingapp.entity.Trade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tradeService.executeTrade(testUser, "ETHUSDT", "BUY", quantity);

        // Assert
        assertEquals(quantity, ethWallet.getAvailableBalance());
    }
}
