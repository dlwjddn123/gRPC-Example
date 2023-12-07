package com.coin.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class PurchasedCoin {

    @Id
    @GeneratedValue
    @Column(name = "purchased_coin_id")
    private Long id;
    private String name;
    private String code;
    private int count;
    private double totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public PurchasedCoin(String name, String code, int count, Member member, double tradePrice) {
        this.name = name;
        this.code = code;
        this.count = count;
        this.member = member;
        this.totalAmount = tradePrice * count;
    }

    public void purchaseCoin(double tradePrice, int count) {
        this.count += count;
        this.totalAmount += tradePrice * count;
    }

    public void sellCoin(double tradePrice, int count) {
        this.count -= count;
        this.totalAmount -= tradePrice * count;
        if (this.count == 0) {

        }
    }

    public double getProfit(double tradePrice) {
        double totalAmountWithTradePrice = tradePrice * count;
        double profitAmount = totalAmountWithTradePrice - totalAmount;
        return profitAmount / totalAmount * 100;
    }

    public double getProfitAndLoss(double tradePrice) {
        double totalAmountWithTradePrice = tradePrice * count;
        return totalAmountWithTradePrice - totalAmount;
    }

    public double getAveragePrice() {
        return totalAmount / count;
    }

    public double getAssessmentAmount(double tradePrice) {
        return tradePrice * count;
    }

}
