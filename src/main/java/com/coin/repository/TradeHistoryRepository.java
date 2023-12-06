package com.coin.repository;

import com.coin.domain.Member;
import com.coin.domain.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    List<TradeHistory> findByMember(Member member);
}
