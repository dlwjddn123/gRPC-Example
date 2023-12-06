package com.coin.repository;

import com.coin.domain.PurchasedCoin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchasedCoinRepository extends JpaRepository<PurchasedCoin, Long> {
    Optional<PurchasedCoin> findByName(String name);
}
