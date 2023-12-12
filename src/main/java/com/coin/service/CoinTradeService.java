package com.coin.service;

import com.coin.CoinServiceGrpc;
import com.coin.GetCoinTradePriceRequest;
import com.coin.GetCoinTradePriceResponse;
import com.coin.auth.SecurityUtils;
import com.coin.domain.*;
import com.coin.repository.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class CoinTradeService {

    private final MemberRepository memberRepository;
    private final PurchasedCoinRepository purchasedCoinRepository;
    private final CoinRepository coinRepository;
    private final TradeHistoryRepository tradeHistoryRepository;

    public String purchaseCoin(String coinName, int count) {
        try {
            Member member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            Coin coin = coinRepository.findByName(coinName).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 존재하지 않는 코인입니다."));
            Optional<PurchasedCoin> purchasedCoin = purchasedCoinRepository.findByName(coinName);
            Integer tradePrice = getTradePrice(coin.getCode());
            if (purchasedCoin.isEmpty()) {
                PurchasedCoin newPurchasedCoin = purchasedCoinRepository.save(PurchasedCoin.builder()
                        .code(coin.getCode())
                        .name(coin.getName())
                        .count(count)
                        .tradePrice(tradePrice)
                        .member(member)
                        .build());
                tradeHistoryRepository.save(TradeHistory.builder()
                        .coinName(newPurchasedCoin.getName())
                        .type(TradeHistory.Type.PURCHASE)
                        .tradeAmount((double) tradePrice * count)
                        .member(member)
                        .count(count).build());
                return "정상적으로 구매되었습니다.\n";
            }
            purchasedCoin.get().purchaseCoin(tradePrice, count);
            tradeHistoryRepository.save(TradeHistory.builder()
                    .coinName(purchasedCoin.get().getName())
                    .type(TradeHistory.Type.PURCHASE)
                    .tradeAmount((double) tradePrice * count)
                    .member(member)
                    .count(count).build());
            return "정상적으로 구매되었습니다.\n";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String sellCoin(String coinName, int count) {
        try {
            Member member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            Coin coin = coinRepository.findByName(coinName).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 존재하지 않는 코인입니다."));
            PurchasedCoin purchasedCoin = purchasedCoinRepository.findByName(coinName).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 보유하고 계신 코인이 없습니다."));
            Integer tradePrice = getTradePrice(coin.getCode());
            if (purchasedCoin.getCount() < count) {
                throw new IllegalArgumentException("[ERROR] 매도할 코인의 개수가 보유한 코인의 개수보다 큽니다. \n" +
                        "현재 보유한 " + purchasedCoin.getName() + "의 개수는 " + purchasedCoin.getCount() + "개 입니다.");
            }
            if (purchasedCoin.getCount() == count) {
                tradeHistoryRepository.save(TradeHistory.builder()
                        .coinName(purchasedCoin.getName())
                        .type(TradeHistory.Type.SELL)
                        .tradeAmount(purchasedCoin.getAssessmentAmount(tradePrice))
                        .member(member)
                        .count(count).build());
                purchasedCoinRepository.delete(purchasedCoin);
                return "정상적으로 판매되었습니다.\n";
            }
            tradeHistoryRepository.save(TradeHistory.builder()
                    .coinName(purchasedCoin.getName())
                    .type(TradeHistory.Type.SELL)
                    .tradeAmount(tradePrice * count)
                    .member(member)
                    .count(count).build());
            purchasedCoin.sellCoin(tradePrice, count);
            return "정상적으로 판매되었습니다.\n";
        } catch (IllegalArgumentException e) {
            return e.getMessage() + "\n";
        }
    }

    @Transactional(readOnly = true)
    public Integer getTradePrice(String code) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        CoinServiceGrpc.CoinServiceBlockingStub stub = CoinServiceGrpc.newBlockingStub(channel);

        GetCoinTradePriceResponse response = stub.getCoinTradePrice(GetCoinTradePriceRequest.newBuilder()
                .setCodes(code)
                .build());

        List<Integer> tradePrices = List.of(response.getTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(p -> Integer.parseInt(p)).collect(Collectors.toList());
        channel.shutdown();
        return tradePrices.get(0);
    }

    @Transactional(readOnly = true)
    public String getHoldings() {
        try {
            Member member = memberRepository.findByLoginIdFetchPurchasedCoins(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            List<PurchasedCoin> purchasedCoins = member.getPurchasedCoins();
            StringBuilder resultBuilder = new StringBuilder();
            if (purchasedCoins.isEmpty()) {
                return "보유하고 계신 코인이 없습니다.\n";
            }
            DecimalFormat df = new DecimalFormat("###,###");
            resultBuilder.append("▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶ 보유 자산 ◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀\n\n");
            resultBuilder.append("----------------------------------------------------------------------------\n\n");
            double totalPurchasePrice = 0;
            double totalAssessmentPrice = 0;
            double totalProfitAndLoss = 0;
            for (PurchasedCoin purchasedCoin : purchasedCoins) {
                Integer tradePrice = getTradePrice(purchasedCoin.getCode());
                resultBuilder.append(purchasedCoin.getName() + "\n\n");
                resultBuilder.append("매수 금액 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getTotalAmount())) + " KRW\n");
                resultBuilder.append("평가 금액 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getAssessmentAmount(tradePrice))) + " KRW\n");
                resultBuilder.append("매수 평균가 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getAveragePrice())) + " KRW\n");
                resultBuilder.append("현재가 : " + df.format(BigInteger.valueOf(tradePrice.longValue())) + " KRW\n");
                resultBuilder.append("평가 손익 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getProfitAndLoss(tradePrice))) + " KRW\n");
                resultBuilder.append("수익률 : " + String.format("%.2f", purchasedCoin.getProfit(tradePrice)) + "%\n\n");
                resultBuilder.append("----------------------------------------------------------------------------\n\n");
                totalPurchasePrice += purchasedCoin.getTotalAmount();
                totalAssessmentPrice += purchasedCoin.getAssessmentAmount(tradePrice);
                totalProfitAndLoss += purchasedCoin.getProfitAndLoss(tradePrice);
            }
            double totalProfit = (totalAssessmentPrice - totalPurchasePrice) / totalPurchasePrice * 100;
            resultBuilder.append("총 매수 금액 : " + df.format(BigInteger.valueOf((long) totalPurchasePrice)) + " KRW\n");
            resultBuilder.append("총 평가 금액 : " + df.format(BigInteger.valueOf((long) totalAssessmentPrice)) + " KRW\n");
            resultBuilder.append("총 평가 손익 : " + df.format(BigInteger.valueOf((long) totalProfitAndLoss)) + " KRW\n");
            resultBuilder.append("총 수익률 : " + String.format("%.2f", totalProfit) + "%\n\n");
            resultBuilder.append("----------------------------------------------------------------------------\n\n");
            return resultBuilder.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String getTradeHistory() {
        try {
            Member member = memberRepository.findByLoginIdFetchTradeHistory(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            List<TradeHistory> tradeHistories = member.getTradeHistories();
            StringBuilder resultBuilder = new StringBuilder();
            DecimalFormat df = new DecimalFormat("###,###");
            resultBuilder.append("▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶ 거래 내역 ◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀\n\n");
            resultBuilder.append("----------------------------------------------------------------------------\n\n");
            for (TradeHistory tradeHistory : tradeHistories) {
                resultBuilder.append(tradeHistory.getCoinName() + "\n");
                resultBuilder.append("종류 : " + tradeHistory.getType().getValue() + "\n");
                resultBuilder.append("체결 금액 : " + df.format(BigInteger.valueOf((long) tradeHistory.getTradeAmount())) + " KRW\n");
                resultBuilder.append("체결 수량 : " + df.format((long) tradeHistory.getCount()) + " 개\n\n");
                resultBuilder.append("----------------------------------------------------------------------------\n\n");
            }
            return resultBuilder.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
