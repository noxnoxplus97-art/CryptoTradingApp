package com.example.tradingapp.repository;

import com.example.tradingapp.entity.CryptoPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {
    @Query("SELECT c FROM CryptoPrice c WHERE c.symbol = :symbol ORDER BY c.timestamp DESC LIMIT 1")
    Optional<CryptoPrice> findLatestBySymbol(String symbol);

    List<CryptoPrice> findBySymbolOrderByTimestampDesc(String symbol);
}
