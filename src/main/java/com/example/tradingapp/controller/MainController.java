package com.example.tradingapp.controller;

import com.example.tradingapp.dto.ApiResponse;
import com.example.tradingapp.dto.PriceResponse;
import com.example.tradingapp.dto.Trade;
import com.example.tradingapp.dto.TradeRequest;
import com.example.tradingapp.dto.Wallet;
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
    public ResponseEntity<ApiResponse<PriceResponse>> getLatestPrice(@PathVariable String symbol) {
        try {
            CryptoPrice price = priceAggregationService.getLatestPrice(symbol);
            PriceResponse response = new PriceResponse();
            response.setSymbol(price.getSymbol());
            response.setBidPrice(price.getBidPrice());
            response.setAskPrice(price.getAskPrice());
            response.setTimestamp(price.getTimestamp().format(dateFormatter));

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error getting price: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Execute a trade (BUY or SELL)
     */
    @PostMapping("/trade")
    public ResponseEntity<ApiResponse<Trade>> executeTrade(@RequestBody TradeRequest request) {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Trade trade = tradeService.executeTrade(user, request.getSymbol(), request.getType(), request.getQuantity());

            return ResponseEntity.ok(ApiResponse.success("Trade executed successfully", trade));
        } catch (Exception e) {
            log.error("Error executing trade: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user's wallet balance
     */
    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<List<Wallet>>> getWalletBalance() {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Wallet> wallets = walletService.getUserWallets(user);

            return ResponseEntity.ok(ApiResponse.success(wallets));
        } catch (Exception e) {
            log.error("Error getting wallet balance: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get user's trading history
     */
    @GetMapping("/trades")
    public ResponseEntity<ApiResponse<List<Trade>>> getTradeHistory() {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Trade> trades = tradeService.getUserTradeHistory(user);

            return ResponseEntity.ok(ApiResponse.success(trades));
        } catch (Exception e) {
            log.error("Error getting trade history: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get trading history for a specific symbol
     */
    @GetMapping("/trades/{symbol}")
    public ResponseEntity<ApiResponse<List<Trade>>> getTradeHistoryBySymbol(@PathVariable String symbol) {
        try {
            User user = userRepository.findById(DEFAULT_USER_ID)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Trade> trades = tradeService.getUserTradeHistoryBySymbol(user, symbol);

            return ResponseEntity.ok(ApiResponse.success(trades));
        } catch (Exception e) {
            log.error("Error getting trade history: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Crypto Trading App is running"));
    }
}
