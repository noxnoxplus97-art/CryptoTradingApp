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
        if (userRepository.findByUsername("testuser").isPresent()) {
            log.info("Default user already exists");
            return;
        }

        // Create default user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@trading.com");
        User savedUser = userRepository.save(user);
        log.info("Created default user: {}", savedUser.getId());

        // Initialize USDT wallet with 50,000 balance
        Wallet usdtWallet = new Wallet();
        usdtWallet.setUser(savedUser);
        usdtWallet.setCurrency("USDT");
        usdtWallet.setBalance(new BigDecimal("50000"));
        usdtWallet.setAvailableBalance(new BigDecimal("50000"));
        walletRepository.save(usdtWallet);
        log.info("Created USDT wallet with balance 50000");

        // Initialize ETH wallet with 0 balance
        Wallet ethWallet = new Wallet();
        ethWallet.setUser(savedUser);
        ethWallet.setCurrency("ETH");
        ethWallet.setBalance(BigDecimal.ZERO);
        ethWallet.setAvailableBalance(BigDecimal.ZERO);
        walletRepository.save(ethWallet);
        log.info("Created ETH wallet with balance 0");

        // Initialize BTC wallet with 0 balance
        Wallet btcWallet = new Wallet();
        btcWallet.setUser(savedUser);
        btcWallet.setCurrency("BTC");
        btcWallet.setBalance(BigDecimal.ZERO);
        btcWallet.setAvailableBalance(BigDecimal.ZERO);
        walletRepository.save(btcWallet);
        log.info("Created BTC wallet with balance 0");
    }
}
