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
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            Coin coin = coinRepository.findByName(coinName).orElseThrow(
                    () -> new IllegalArgumentException("존재하지 않는 코인입니다."));
            Optional<PurchasedCoin> purchasedCoin = purchasedCoinRepository.findByName(coinName);
            Double tradePrice = getTradePrice(coin.getCode());
            if (purchasedCoin.isEmpty()) {
                purchasedCoinRepository.save(PurchasedCoin.builder()
                        .code(coin.getCode())
                        .name(coin.getName())
                        .count(count)
                        .tradePrice(tradePrice)
                        .member(member)
                        .build());
                return "정상적으로 구매되었습니다.\n";
            }
            purchasedCoin.get().purchaseCoin(tradePrice, count);
            return "정상적으로 구매되었습니다.\n";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String sellCoin(String coinName, int count) {
        try {
            Member member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            Coin coin = coinRepository.findByName(coinName).orElseThrow(
                    () -> new IllegalArgumentException("존재하지 않는 코인입니다."));
            PurchasedCoin purchasedCoin = purchasedCoinRepository.findByName(coinName).orElseThrow(
                    () -> new IllegalArgumentException("보유하고 계신 코인이 없습니다."));
            Double tradePrice = getTradePrice(coin.getCode());
            if (purchasedCoin.getCount() < count) {
                throw new IllegalArgumentException("매도할 코인의 개수가 보유한 코인의 개수보다 큽니다.");
            }
            if (purchasedCoin.getCount() == count) {
                tradeHistoryRepository.save(new TradeHistory(coin.getName(), (int) purchasedCoin.getProfitAndLoss(tradePrice), member));
                purchasedCoinRepository.delete(purchasedCoin);
                return "정상적으로 판매되었습니다.\n";
            }
            purchasedCoin.sellCoin(tradePrice, count);
            return "정상적으로 판매되었습니다.\n";
        } catch (IllegalArgumentException e) {
            return e.getMessage() + "\n";
        }
    }

    @Transactional(readOnly = true)
    public Double getTradePrice(String code) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        CoinServiceGrpc.CoinServiceBlockingStub stub = CoinServiceGrpc.newBlockingStub(channel);

        GetCoinTradePriceResponse response = stub.getCoinTradePrice(GetCoinTradePriceRequest.newBuilder()
                .setCodes(code)
                .build());

        List<String> coinPrices = List.of(response.getResult().replace("[", "").replace("]", "").replaceAll("'", "").replace("원", ""));
        String[] split = coinPrices.get(0).split(":");

        channel.shutdown();
        return Double.parseDouble(split[1].trim());
    }

    @Transactional(readOnly = true)
    public String getHoldings() {
        try {
            Member member = memberRepository.findByLoginIdFetchPurchasedCoins(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            List<PurchasedCoin> purchasedCoins = member.getPurchasedCoins();
            StringBuilder resultBuilder = new StringBuilder();
            if (purchasedCoins.isEmpty()) {
                return "보유하고 계신 코인이 없습니다.\n";
            }
            DecimalFormat df = new DecimalFormat("###,###");
            resultBuilder.append("▶▶▶▶▶▶▶▶▶▶ 보유 자산 ◀◀◀◀◀◀◀◀◀◀\\n\"");
            for (PurchasedCoin purchasedCoin : purchasedCoins) {
                Double tradePrice = getTradePrice(purchasedCoin.getCode());
                resultBuilder.append(purchasedCoin.getName() + "\n");
                resultBuilder.append("총 매수 금액 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getTotalAmount())) + "원\n");
                resultBuilder.append("평단가 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getAveragePrice())) + "원\n");
                resultBuilder.append("현재가 : " + df.format(BigInteger.valueOf(tradePrice.longValue())) + "원\n");
                resultBuilder.append("평가 손익 : " + df.format(BigInteger.valueOf((long) purchasedCoin.getProfitAndLoss(tradePrice))) + "\n");
                resultBuilder.append("수익률 : " + String.format("%.2f", purchasedCoin.getProfit(tradePrice)) + "%\n");
            }
            resultBuilder.append("-----------------------------\n");
            return resultBuilder.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @Transactional(readOnly = true)
    public String getTradeHistory() {
        try {
            Member member = memberRepository.findByLoginIdFetchTradeHistory(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            List<TradeHistory> tradeHistories = member.getTradeHistories();
            StringBuilder resultBuilder = new StringBuilder();
            DecimalFormat df = new DecimalFormat("###,###");
            resultBuilder.append("▶▶▶▶▶▶▶▶▶▶ 거래 내역 ◀◀◀◀◀◀◀◀◀◀\n\n");
            for (TradeHistory tradeHistory : tradeHistories) {
                resultBuilder.append(tradeHistory.getCoinName() + "\n");
                resultBuilder.append("실현 손익 : " + df.format(tradeHistory.getRealizedProfitAndLoss()) + "\n\n");
            }
            resultBuilder.append("-----------------------------\n");
            return resultBuilder.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
