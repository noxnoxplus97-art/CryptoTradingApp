package com.example.tradingapp.service;

import com.example.tradingapp.dto.Wallet;
import com.example.tradingapp.entity.User;
import com.example.tradingapp.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public List<Wallet> getUserWallets(User user) {
        List<com.example.tradingapp.entity.Wallet> wallets = walletRepository.findByUser(user);
        return wallets.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public Wallet getWalletByCurrency(User user, String currency) {
        com.example.tradingapp.entity.Wallet wallet = walletRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found for currency: " + currency));
        return mapToDto(wallet);
    }

    private Wallet mapToDto(com.example.tradingapp.entity.Wallet entity) {
        Wallet dto = new Wallet();
        dto.setId(entity.getId());
        dto.setCurrency(entity.getCurrency());
        dto.setBalance(entity.getBalance());
        dto.setAvailableBalance(entity.getAvailableBalance());
        return dto;
    }
}
