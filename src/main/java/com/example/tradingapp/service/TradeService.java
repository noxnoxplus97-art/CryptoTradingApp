package com.example.tradingapp.service;

import com.example.tradingapp.dto.Trade;
import com.example.tradingapp.entity.User;
import com.example.tradingapp.entity.Wallet;
import com.example.tradingapp.repository.TradeRepository;
import com.example.tradingapp.repository.WalletRepository;
import com.example.tradingapp.repository.CryptoPriceRepository;
import com.example.tradingapp.entity.CryptoPrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    @Transactional
    public Trade executeTrade(User user, String symbol, String tradeType, BigDecimal quantity) {
        // Validate symbol
        if (!isValidSymbol(symbol)) {
            throw new IllegalArgumentException("Invalid trading symbol: " + symbol);
        }

        // Get latest price
        CryptoPrice price = cryptoPriceRepository.findLatestBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("No price data available for symbol: " + symbol));

        BigDecimal tradePrice;
        BigDecimal totalAmount;

        if ("BUY".equalsIgnoreCase(tradeType)) {
            // For BUY, use ask price
            tradePrice = price.getAskPrice();
            totalAmount = quantity.multiply(tradePrice);

            // Check if user has enough USDT
            Wallet usdtWallet = walletRepository.findByUserAndCurrency(user, "USDT")
                    .orElseThrow(() -> new IllegalArgumentException("USDT wallet not found"));

            if (usdtWallet.getAvailableBalance().compareTo(totalAmount) < 0) {
                throw new IllegalArgumentException("Insufficient USDT balance");
            }

            // Deduct USDT
            usdtWallet.setAvailableBalance(usdtWallet.getAvailableBalance().subtract(totalAmount));
            usdtWallet.setBalance(usdtWallet.getBalance().subtract(totalAmount));

            // Add crypto to wallet
            Wallet cryptoWallet = walletRepository.findByUserAndCurrency(user, extractCurrencyFromSymbol(symbol))
                    .orElseGet(() -> createNewWallet(user, extractCurrencyFromSymbol(symbol)));

            cryptoWallet.setBalance(cryptoWallet.getBalance().add(quantity));
            cryptoWallet.setAvailableBalance(cryptoWallet.getAvailableBalance().add(quantity));

            walletRepository.save(usdtWallet);
            walletRepository.save(cryptoWallet);

        } else if ("SELL".equalsIgnoreCase(tradeType)) {
            // For SELL, use bid price
            tradePrice = price.getBidPrice();
            totalAmount = quantity.multiply(tradePrice);

            // Check if user has enough crypto
            String cryptoCurrency = extractCurrencyFromSymbol(symbol);
            Wallet cryptoWallet = walletRepository.findByUserAndCurrency(user, cryptoCurrency)
                    .orElseThrow(() -> new IllegalArgumentException("Insufficient " + cryptoCurrency + " balance"));

            if (cryptoWallet.getAvailableBalance().compareTo(quantity) < 0) {
                throw new IllegalArgumentException("Insufficient " + cryptoCurrency + " balance");
            }

            // Deduct crypto
            cryptoWallet.setBalance(cryptoWallet.getBalance().subtract(quantity));
            cryptoWallet.setAvailableBalance(cryptoWallet.getAvailableBalance().subtract(quantity));

            // Add USDT to wallet
            Wallet usdtWallet = walletRepository.findByUserAndCurrency(user, "USDT")
                    .orElseGet(() -> createNewWallet(user, "USDT"));

            usdtWallet.setBalance(usdtWallet.getBalance().add(totalAmount));
            usdtWallet.setAvailableBalance(usdtWallet.getAvailableBalance().add(totalAmount));

            walletRepository.save(cryptoWallet);
            walletRepository.save(usdtWallet);

        } else {
            throw new IllegalArgumentException("Invalid trade type: " + tradeType);
        }

        // Save trade record
        com.example.tradingapp.entity.Trade tradeEntity = new com.example.tradingapp.entity.Trade();
        tradeEntity.setUser(user);
        tradeEntity.setSymbol(symbol);
        tradeEntity.setType(com.example.tradingapp.entity.Trade.TradeType.valueOf(tradeType.toUpperCase()));
        tradeEntity.setQuantity(quantity);
        tradeEntity.setPrice(tradePrice);
        tradeEntity.setTotalAmount(totalAmount);
        tradeEntity.setTimestamp(LocalDateTime.now());
        tradeEntity.setStatus("COMPLETED");

        com.example.tradingapp.entity.Trade savedTrade = tradeRepository.save(tradeEntity);

        return mapToDto(savedTrade);
    }

    public List<Trade> getUserTradeHistory(User user) {
        List<com.example.tradingapp.entity.Trade> trades = tradeRepository.findByUserOrderByTimestampDesc(user);
        return trades.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<Trade> getUserTradeHistoryBySymbol(User user, String symbol) {
        List<com.example.tradingapp.entity.Trade> trades = tradeRepository.findByUserAndSymbolOrderByTimestampDesc(user, symbol);
        return trades.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private Trade mapToDto(com.example.tradingapp.entity.Trade entity) {
        Trade dto = new Trade();
        dto.setId(entity.getId());
        dto.setSymbol(entity.getSymbol());
        dto.setType(entity.getType().toString());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPrice());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setTimestamp(entity.getTimestamp());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    private boolean isValidSymbol(String symbol) {
        return "ETHUSDT".equalsIgnoreCase(symbol) || "BTCUSDT".equalsIgnoreCase(symbol);
    }

    private String extractCurrencyFromSymbol(String symbol) {
        return symbol.replace("USDT", "").toUpperCase();
    }

    private Wallet createNewWallet(User user, String currency) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        return wallet;
    }
}
