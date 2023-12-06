package com.coin.repository;

import com.coin.domain.Member;
import com.coin.domain.WatchedCoin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchedCoinRepository extends JpaRepository<WatchedCoin, Long> {
    List<WatchedCoin> findByMember(Member member);

    Optional<WatchedCoin> findByName(String name);
}
