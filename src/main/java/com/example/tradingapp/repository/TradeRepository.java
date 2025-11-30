package com.example.tradingapp.repository;

import com.example.tradingapp.entity.Trade;
import com.example.tradingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserOrderByTimestampDesc(User user);

    List<Trade> findByUserAndSymbolOrderByTimestampDesc(User user, String symbol);
}
