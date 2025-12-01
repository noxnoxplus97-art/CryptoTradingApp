package com.example.tradingapp.config;

import com.example.tradingapp.entity.User;
import com.example.tradingapp.entity.Wallet;
import com.example.tradingapp.repository.UserRepository;
import com.example.tradingapp.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

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
}
