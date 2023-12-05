package com.coin.repository;

import com.coin.domain.PurchasedCoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchasedCoinRepository extends JpaRepository<PurchasedCoin, Long> {
}
