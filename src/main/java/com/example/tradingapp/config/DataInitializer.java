package com.example.tradingapp.config;

import com.example.tradingapp.entity.User;
import com.example.tradingapp.entity.Wallet;
import com.example.tradingapp.entity.CryptoPrice;
import com.example.tradingapp.repository.UserRepository;
import com.example.tradingapp.repository.WalletRepository;
import com.example.tradingapp.repository.CryptoPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultUser();
    }

    private void initializeDefaultUser() {
        // Check if default user already exists
        var existingUser = userRepository.findByUsername("testuser");
        if (existingUser.isPresent()) {
            log.info("Default user already exists with ID: {}", existingUser.get().getId());
            return;
        }

        // Create default user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@trading.com");
        User savedUser = userRepository.save(user);
        log.info("Created default user: {}", savedUser.getId());

        // Initialize USDT wallet with 50,000 balance (only if not exists)
        initializeWalletIfNotExists(savedUser, "USDT", new BigDecimal("50000"));

        // Initialize ETH wallet with 0 balance (only if not exists)
        initializeWalletIfNotExists(savedUser, "ETH", BigDecimal.ZERO);

        // Initialize BTC wallet with 0 balance (only if not exists)
        initializeWalletIfNotExists(savedUser, "BTC", BigDecimal.ZERO);
    }

    private void initializeWalletIfNotExists(User user, String currency, BigDecimal defaultBalance) {
        // Check if wallet already exists for this user and currency
        var existingWallet = walletRepository.findByUserAndCurrency(user, currency);
        
        if (existingWallet.isPresent()) {
            log.info("Wallet for {} already exists with balance: {}, skipping initialization", 
                currency, existingWallet.get().getBalance());
            return;
        }

        // Create new wallet with default balance
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency);
        wallet.setBalance(defaultBalance);
        wallet.setAvailableBalance(defaultBalance);
        walletRepository.save(wallet);
        log.info("Created {} wallet with balance {}", currency, defaultBalance);
    }

    /*
    IF INITIALIZE INTERNAL PRICES(TOTALLY NO 3RD CALLS IS NEEDED):
    
    private void initializePrices() {
        // Check if prices already exist in database
        var existingEthPrice = cryptoPriceRepository.findLatestBySymbol("ETHUSDT");
        var existingBtcPrice = cryptoPriceRepository.findLatestBySymbol("BTCUSDT");
        
        if (existingEthPrice.isPresent() && existingBtcPrice.isPresent()) {
            log.info("Prices already initialized, skipping price seeding");
            return;
        }
        
        // Initialize ETHUSDT if not exists
        if (existingEthPrice.isEmpty()) {
            CryptoPrice ethPrice = new CryptoPrice();
            ethPrice.setSymbol("ETHUSDT");
            ethPrice.setBidPrice(new BigDecimal("2500.00")); // Initial bid price for ETH
            ethPrice.setAskPrice(new BigDecimal("2501.00")); // Initial ask price for ETH
            ethPrice.setTimestamp(LocalDateTime.now());
            ethPrice.setSource("INTERNAL"); // Mark as internally generated
            cryptoPriceRepository.save(ethPrice);
            log.info("Initialized ETHUSDT price: Bid={}, Ask={}", 
                ethPrice.getBidPrice(), ethPrice.getAskPrice());
        }
        
        // Initialize BTCUSDT if not exists
        if (existingBtcPrice.isEmpty()) {
            CryptoPrice btcPrice = new CryptoPrice();
            btcPrice.setSymbol("BTCUSDT");
            btcPrice.setBidPrice(new BigDecimal("50000.00")); // Initial bid price for BTC
            btcPrice.setAskPrice(new BigDecimal("50100.00")); // Initial ask price for BTC
            btcPrice.setTimestamp(LocalDateTime.now());
            btcPrice.setSource("INTERNAL"); // Mark as internally generated
            cryptoPriceRepository.save(btcPrice);
            log.info("Initialized BTCUSDT price: Bid={}, Ask={}", 
                btcPrice.getBidPrice(), btcPrice.getAskPrice());
        }
    }
    */
}
