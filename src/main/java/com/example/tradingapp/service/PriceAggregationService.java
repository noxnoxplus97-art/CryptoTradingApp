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

    /*
    IF INTERNAL PRICE GENERATION
    private void generateInternalPrices() {
        log.debug("Generating internal prices...");
        
        try {
            // Process each relevant symbol
            String[] symbols = {"ETHUSDT", "BTCUSDT"};
            
            for (String symbol : symbols) {
                // 1. Get the last recorded price for this symbol
                java.util.Optional<CryptoPrice> lastPriceOpt = cryptoPriceRepository.findLatestBySymbol(symbol);
                
                if (lastPriceOpt.isPresent()) {
                    CryptoPrice lastPrice = lastPriceOpt.get();
                    
                    // 2. Generate small random change between -2% and +2%
                    double changePercent = (Math.random() * 4.0) - 2.0; // Range: -2% to +2%
                    BigDecimal changeMultiplier = BigDecimal.ONE.add(
                        new BigDecimal(changePercent).divide(new BigDecimal("100"), 8, java.math.RoundingMode.HALF_UP)
                    );
                    
                    // 3. Calculate new bid and ask prices
                    BigDecimal newBidPrice = lastPrice.getBidPrice()
                        .multiply(changeMultiplier)
                        .setScale(8, java.math.RoundingMode.HALF_UP);
                    
                    BigDecimal newAskPrice = lastPrice.getAskPrice()
                        .multiply(changeMultiplier)
                        .setScale(8, java.math.RoundingMode.HALF_UP);
                    
                    // 4. Ensure ask price is always higher than bid (realistic spread)
                    BigDecimal spreadAmount = new BigDecimal("1.00"); // Fixed spread of 1 unit
                    if (newAskPrice.compareTo(newBidPrice) <= 0) {
                        newAskPrice = newBidPrice.add(spreadAmount);
                    }
                    
                    // 5. Create and save new internal price record
                    CryptoPrice internalPrice = new CryptoPrice();
                    internalPrice.setSymbol(symbol);
                    internalPrice.setBidPrice(newBidPrice);
                    internalPrice.setAskPrice(newAskPrice);
                    internalPrice.setTimestamp(LocalDateTime.now());
                    internalPrice.setSource("INTERNAL"); // Mark as internally generated
                    
                    // 6. Save to database
                    cryptoPriceRepository.save(internalPrice);
                    
                    log.debug("Generated internal price for {}: Bid={}, Ask={}", 
                        symbol, newBidPrice, newAskPrice);
                    
                } else {
                    log.warn("No price history found for symbol: {}. Please initialize prices via DataInitializer.", symbol);
                }
            }
            
        } catch (Exception e) {
            log.error("Error generating internal prices: ", e);
        }
    }
    */
}
