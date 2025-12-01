package com.example.tradingapp.service;

import com.example.tradingapp.dto.WalletDTO;
import com.example.tradingapp.entity.Wallet;
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

    public List<WalletDTO> getUserWallets(User user) {
        List<Wallet> wallets = walletRepository.findByUser(user);
        return wallets.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public WalletDTO getWalletByCurrency(User user, String currency) {
        Wallet wallet = walletRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new RuntimeException("Wallet not found for currency: " + currency));
        return mapToDto(wallet);
    }

    private WalletDTO mapToDto(Wallet entity) {
        WalletDTO dto = new WalletDTO();
        dto.setId(entity.getId());
        dto.setCurrency(entity.getCurrency());
        dto.setBalance(entity.getBalance());
        dto.setAvailableBalance(entity.getAvailableBalance());
        return dto;
    }
}
