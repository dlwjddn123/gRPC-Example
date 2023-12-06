package com.coin.repository;

import com.coin.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String nickname);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.purchasedCoins mp WHERE m.loginId = :loginId")
    Optional<Member> findByLoginIdFetchPurchasedCoins(@Param("loginId") String loginId);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.tradeHistories mt WHERE m.loginId = :loginId")
    Optional<Member> findByLoginIdFetchTradeHistory(@Param("loginId") String loginId);
}
