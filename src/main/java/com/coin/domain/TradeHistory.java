package com.coin.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class TradeHistory {


    public enum Type {
        SELL("매도"), PURCHASE("매수");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "trade_history_id")
    private Long id;
    private String coinName;
    private double tradeAmount;
    private double count;
    @Enumerated(value = EnumType.STRING)
    private Type type;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder

    public TradeHistory(String coinName, double tradeAmount, double count, Type type, Member member) {
        this.coinName = coinName;
        this.tradeAmount = tradeAmount;
        this.count = count;
        this.type = type;
        this.member = member;
    }
}
