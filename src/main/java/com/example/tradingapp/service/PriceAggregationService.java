package com.example.tradingapp.service;

import com.example.tradingapp.entity.CryptoPrice;
import com.example.tradingapp.repository.CryptoPriceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PriceAggregationService {

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, CryptoPrice> bestPricesCache = new ConcurrentHashMap<>();

    // Run every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void aggregatePrices() {
        log.info("Starting price aggregation...");

        try {
            // Fetch from Binance
            fetchBinancePrices();

            // Fetch from Huobi
            fetchHuobiPrices();

            log.info("Price aggregation completed");
        } catch (Exception e) {
            log.error("Error during price aggregation: ", e);
        }
    }

    private void fetchBinancePrices() {
        try {
            String url = "https://api.binance.com/api/v3/ticker/bookTicker";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonArray = objectMapper.readTree(response);

            if (jsonArray.isArray()) {
                for (JsonNode item : jsonArray) {
                    String symbol = item.get("symbol").asText();
                    if (isRelevantSymbol(symbol)) {
                        BigDecimal bidPrice = new BigDecimal(item.get("bidPrice").asText());
                        BigDecimal askPrice = new BigDecimal(item.get("askPrice").asText());

                        processPriceData(symbol, bidPrice, askPrice, "BINANCE");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Binance prices: ", e);
        }
    }

    private void fetchHuobiPrices() {
        try {
            String url = "https://api.huobi.pro/market/tickers";
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response);

            if (jsonResponse.has("data") && jsonResponse.get("data").isArray()) {
                for (JsonNode item : jsonResponse.get("data")) {
                    String symbol = item.get("symbol").asText().toUpperCase();
                    if (isRelevantSymbol(symbol)) {
                        // Huobi uses different naming convention and provides bid/ask
                        BigDecimal bid = new BigDecimal(item.get("bid").asText());
                        BigDecimal ask = new BigDecimal(item.get("ask").asText());

                        processPriceData(symbol, bid, ask, "HUOBI");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching Huobi prices: ", e);
        }
    }

    private void processPriceData(String symbol, BigDecimal bidPrice, BigDecimal askPrice, String source) {
        CryptoPrice existingBestPrice = bestPricesCache.get(symbol);

        // Create new price entry
        CryptoPrice newPrice = new CryptoPrice();
        newPrice.setSymbol(symbol);
        newPrice.setBidPrice(bidPrice);
        newPrice.setAskPrice(askPrice);
        newPrice.setTimestamp(LocalDateTime.now());
        newPrice.setSource(source);

        boolean shouldUpdate = false;

        if (existingBestPrice == null) {
            // First price, always save
            shouldUpdate = true;
        } else {
            // Compare bid and ask prices
            // For BUY (ask price): lower is better for buyers
            // For SELL (bid price): higher is better for sellers
            int askComparison = newPrice.getAskPrice().compareTo(existingBestPrice.getAskPrice());
            int bidComparison = newPrice.getBidPrice().compareTo(existingBestPrice.getBidPrice());

            // Update if new ask is lower (better for buyers) or bid is higher (better for sellers)
            if (askComparison < 0 || bidComparison > 0) {
                shouldUpdate = true;
            }
        }

        if (shouldUpdate) {
            bestPricesCache.put(symbol, newPrice);
            cryptoPriceRepository.save(newPrice);
            log.debug("Saved best price for {}: Bid={}, Ask={} from {}", symbol, bidPrice, askPrice, source);
        }
    }

    private boolean isRelevantSymbol(String symbol) {
        String upperSymbol = symbol.toUpperCase();
        return upperSymbol.equals("ETHUSDT") || upperSymbol.equals("BTCUSDT");
    }

    public CryptoPrice getLatestPrice(String symbol) {
        return cryptoPriceRepository.findLatestBySymbol(symbol)
                .orElseThrow(() -> new RuntimeException("No price data available for symbol: " + symbol));
    }
}
