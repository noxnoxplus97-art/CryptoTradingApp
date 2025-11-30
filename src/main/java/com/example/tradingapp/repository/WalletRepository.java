package com.example.tradingapp.repository;

import com.example.tradingapp.entity.Wallet;
import com.example.tradingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByUserAndCurrency(User user, String currency);

    List<Wallet> findByUser(User user);
}
