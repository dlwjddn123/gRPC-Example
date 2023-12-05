package com.coin.repository;

import com.coin.domain.WatchedCoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchedCoinRepository extends JpaRepository<WatchedCoin, Long> {
}
