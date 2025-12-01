package com.example.tradingapp.controller;

import com.example.tradingapp.dto.ApiResponse;
import com.example.tradingapp.dto.PriceResponse;
import com.example.tradingapp.dto.Trade;
import com.example.tradingapp.dto.TradeRequest;
import com.example.tradingapp.dto.Wallet;
import com.example.tradingapp.entity.CryptoPrice;
import com.example.tradingapp.entity.User;
import com.example.tradingapp.repository.UserRepository;
import com.example.tradingapp.service.PriceAggregationService;
import com.example.tradingapp.service.TradeService;
import com.example.tradingapp.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private TradeService tradeService;

    @Mock
    private WalletService walletService;

    @Mock
    private PriceAggregationService priceAggregationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MainController mainController;

    private User testUser;
    private CryptoPrice ethPrice;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        ethPrice = new CryptoPrice();
        ethPrice.setId(1L);
        ethPrice.setSymbol("ETHUSDT");
        ethPrice.setAskPrice(new BigDecimal("3000"));
        ethPrice.setBidPrice(new BigDecimal("2999"));
        ethPrice.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testGetLatestPrice_Success() {
        // Arrange
        when(priceAggregationService.getLatestPrice("ETHUSDT"))
                .thenReturn(ethPrice);

        // Act
        ResponseEntity<ApiResponse<PriceResponse>> response = mainController.getLatestPrice("ETHUSDT");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("ETHUSDT", response.getBody().getData().getSymbol());
        assertEquals(new BigDecimal("3000"), response.getBody().getData().getAskPrice());
        assertEquals(new BigDecimal("2999"), response.getBody().getData().getBidPrice());

        verify(priceAggregationService).getLatestPrice("ETHUSDT");
    }

    @Test
    void testGetLatestPrice_Error() {
        // Arrange
        when(priceAggregationService.getLatestPrice("INVALID"))
                .thenThrow(new RuntimeException("Price not found"));

        // Act
        ResponseEntity<ApiResponse<PriceResponse>> response = mainController.getLatestPrice("INVALID");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Price not found"));
    }

    @Test
    void testExecuteTrade_Success() {
        // Arrange
        TradeRequest request = new TradeRequest();
        request.setSymbol("ETHUSDT");
        request.setType("BUY");
        request.setQuantity(new BigDecimal("1"));

        Trade tradeDto = new Trade();
        tradeDto.setId(1L);
        tradeDto.setSymbol("ETHUSDT");
        tradeDto.setType("BUY");
        tradeDto.setQuantity(new BigDecimal("1"));
        tradeDto.setPrice(new BigDecimal("3000"));
        tradeDto.setTotalAmount(new BigDecimal("3000"));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(tradeService.executeTrade(testUser, "ETHUSDT", "BUY", new BigDecimal("1")))
                .thenReturn(tradeDto);

        // Act
        ResponseEntity<ApiResponse<Trade>> response = mainController.executeTrade(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("ETHUSDT", response.getBody().getData().getSymbol());
        assertEquals("BUY", response.getBody().getData().getType());

        verify(userRepository).findById(1L);
        verify(tradeService).executeTrade(testUser, "ETHUSDT", "BUY", new BigDecimal("1"));
    }

    @Test
    void testExecuteTrade_UserNotFound() {
        // Arrange
        TradeRequest request = new TradeRequest();
        request.setSymbol("ETHUSDT");
        request.setType("BUY");
        request.setQuantity(new BigDecimal("1"));

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<Trade>> response = mainController.executeTrade(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("User not found"));
    }

    @Test
    void testGetWalletBalance_Success() {
        // Arrange
        Wallet usdtWallet = new Wallet();
        usdtWallet.setCurrency("USDT");
        usdtWallet.setBalance(new BigDecimal("50000"));
        usdtWallet.setAvailableBalance(new BigDecimal("50000"));

        Wallet ethWallet = new Wallet();
        ethWallet.setCurrency("ETH");
        ethWallet.setBalance(new BigDecimal("1.5"));
        ethWallet.setAvailableBalance(new BigDecimal("1.5"));

        List<Wallet> wallets = List.of(usdtWallet, ethWallet);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(walletService.getUserWallets(testUser))
                .thenReturn(wallets);

        // Act
        ResponseEntity<ApiResponse<List<Wallet>>> response = mainController.getWalletBalance();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("USDT", response.getBody().getData().get(0).getCurrency());
        assertEquals("ETH", response.getBody().getData().get(1).getCurrency());

        verify(userRepository).findById(1L);
        verify(walletService).getUserWallets(testUser);
    }

    @Test
    void testGetWalletBalance_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<ApiResponse<List<Wallet>>> response = mainController.getWalletBalance();

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("User not found"));
    }

    @Test
    void testGetWalletBalance_EmptyWallets() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(walletService.getUserWallets(testUser))
                .thenReturn(List.of());

        // Act
        ResponseEntity<ApiResponse<List<Wallet>>> response = mainController.getWalletBalance();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(0, response.getBody().getData().size());
    }
}
