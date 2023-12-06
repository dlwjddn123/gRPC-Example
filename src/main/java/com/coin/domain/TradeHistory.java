package com.coin.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class TradeHistory {

    @Id
    @GeneratedValue
    @Column(name = "trade_history_id")
    private Long id;
    private String coinName;
    private int realizedProfitAndLoss;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public TradeHistory(String coinName, int realizedProfitAndLoss, Member member) {
        this.coinName = coinName;
        this.realizedProfitAndLoss = realizedProfitAndLoss;
        this.member = member;
    }
}
