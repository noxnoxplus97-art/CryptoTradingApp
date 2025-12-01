package com.example.tradingapp.controller;

import com.example.tradingapp.dto.ApiResponseDTO;
import com.example.tradingapp.dto.PriceResponseDTO;
import com.example.tradingapp.dto.TradeDTO;
import com.example.tradingapp.dto.TradeRequestDTO;
import com.example.tradingapp.dto.WalletDTO;
import com.example.tradingapp.entity.User;
import com.example.tradingapp.entity.CryptoPrice;
import com.example.tradingapp.repository.UserRepository;
import com.example.tradingapp.service.PriceAggregationService;
import com.example.tradingapp.service.TradeService;
import com.example.tradingapp.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class MainController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PriceAggregationService priceAggregationService;

    @Autowired
    private UserRepository userRepository;

    private static final Long DEFAULT_USER_ID = 1L;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get latest aggregated price for a symbol
     */
    @GetMapping("/price/{symbol}")
    public ResponseEntity<ApiResponseDTO<PriceResponseDTO>> getLatestPrice(@PathVariable String symbol) {
        try {
            CryptoPrice price = priceAggregationService.getLatestPrice(symbol);
            PriceResponseDTO response = new PriceResponseDTO();
            response.setSymbol(price.getSymbol());
            response.setBidPrice(price.getBidPrice());
            response.setAskPrice(price.getAskPrice());
            response.setTimestamp(price.getTimestamp().format(dateFormatter));

            return ResponseEntity.ok(ApiResponseDTO.success(response));
        } catch (Exception e) {
            log.error("Error getting price: ", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * Execute a trade (BUY or SELL)
     */
    @PostMapping("/trade")
    public ResponseEntity<ApiResponseDTO<TradeDTO>> executeTrade(@RequestBody TradeRequestDTO request) {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            TradeDTO trade = tradeService.executeTrade(user, request.getSymbol(), request.getType(), request.getQuantity());

            return ResponseEntity.ok(ApiResponseDTO.success("Trade executed successfully", trade));
        } catch (Exception e) {
            log.error("Error executing trade: ", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * Get user's wallet balance
     */
    @GetMapping("/wallet")
    public ResponseEntity<ApiResponseDTO<List<WalletDTO>>> getWalletBalance() {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<WalletDTO> wallets = walletService.getUserWallets(user);

            return ResponseEntity.ok(ApiResponseDTO.success(wallets));
        } catch (Exception e) {
            log.error("Error getting wallet balance: ", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * Get user's trading history
     */
    @GetMapping("/trades")
    public ResponseEntity<ApiResponseDTO<List<TradeDTO>>> getTradeHistory() {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<TradeDTO> trades = tradeService.getUserTradeHistory(user);

            return ResponseEntity.ok(ApiResponseDTO.success(trades));
        } catch (Exception e) {
            log.error("Error getting trade history: ", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * Get trading history for a specific symbol
     */
    @GetMapping("/trades/{symbol}")
    public ResponseEntity<ApiResponseDTO<List<TradeDTO>>> getTradeHistoryBySymbol(@PathVariable String symbol) {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<TradeDTO> trades = tradeService.getUserTradeHistoryBySymbol(user, symbol);

            return ResponseEntity.ok(ApiResponseDTO.success(trades));
        } catch (Exception e) {
            log.error("Error getting trade history: ", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponseDTO<String>> health() {
        return ResponseEntity.ok(ApiResponseDTO.success("Crypto Trading App is running"));
    }
}
