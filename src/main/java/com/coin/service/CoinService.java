package com.coin.service;


import com.coin.CoinServiceGrpc;
import com.coin.CryptoInfo;
import com.coin.GetCoinTradePriceRequest;
import com.coin.GetCoinTradePriceResponse;
import com.coin.auth.SecurityUtils;
import com.coin.domain.Coin;
import com.coin.domain.Member;
import com.coin.domain.WatchedCoin;
import com.coin.repository.CoinRepository;
import com.coin.repository.MemberRepository;
import com.coin.repository.WatchedCoinRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CoinService {

    private final MemberRepository memberRepository;
    private final WatchedCoinRepository watchedCoinRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CoinRepository coinRepository;

    @Transactional
    public String join(String loginId, String nickname, String accountName, String password) {
        Member member = Member.builder()
                .loginId(loginId)
                .nickname(nickname)
                .account(Member.Account.of(accountName))
                .password(bCryptPasswordEncoder.encode(password))
                .build();

        memberRepository.save(member);
        return "회원 가입이 완료되었습니다.\n";
    }

    @Transactional
    public String addWatchedCoin(String coinNames) {
        try {
            Member member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            for (String coinName : coinNames.split(", ")) {
                Coin coin = coinRepository.findByName(coinName).orElseThrow(
                        () -> new IllegalArgumentException("[ERROR] 존재하지 않는 코인입니다."));
                Optional<WatchedCoin> alreadyExist = watchedCoinRepository.findByName(coin.getName());
                if (alreadyExist.isEmpty()) {
                    watchedCoinRepository.save(new WatchedCoin(coin.getCode(), coin.getName(), member));
                }
            }
            return coinNames + " (이/가) 관심 목록에 추가되었습니다.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    public String getAllCoinsTradePrice() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        List<Coin> coins = coinRepository.findAll();
        List<String> codes = coins.stream().map(c -> c.getCode()).collect(Collectors.toList());
        List<String> names = coins.stream().map(c -> c.getName()).collect(Collectors.toList());

        CoinServiceGrpc.CoinServiceBlockingStub stub = CoinServiceGrpc.newBlockingStub(channel);

        GetCoinTradePriceResponse response = stub.getCoinTradePrice(GetCoinTradePriceRequest.newBuilder()
                .setCodes(codes.toString())
                .setNames(names.toString())
                .build());

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶ 현재가 ◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀\n");
        resultBuilder.append("                                          현재가        전일 대비     거래 대금\n");

        List<Integer> tradePrices = List.of(response.getTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(p -> Integer.parseInt(p)).collect(Collectors.toList());
        List<Double> changeRates = List.of(response.getChangeRates().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(r -> Double.parseDouble(r)).collect(Collectors.toList());
        List<Double> totalTradePrices = List.of(response.getTotalTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(p -> Double.parseDouble(p)).collect(Collectors.toList());
        DecimalFormat df = new DecimalFormat("###,###.##");

        for (int i = 0; i < tradePrices.size(); i++) {
            CryptoInfo cryptoInfo = new CryptoInfo(names.get(i) + " (" + codes.get(i) + ") : " + df.format(tradePrices.get(i)) + " KRW  " + df.format(changeRates.get(i) * 100) + "%  "
                    + df.format(BigInteger.valueOf((long) (totalTradePrices.get(i) / 1000000))) + " 백만");
            resultBuilder.append(cryptoInfo).append("\n");
        }
        resultBuilder.append("----------------------------------------------------------------------------\n\n");
        channel.shutdown();
        return resultBuilder.toString();
    }

    public String getSearchCoinsTradePrice(String coinNames) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        List<Coin> coins = new ArrayList<>();
        for (String coinName : coinNames.split(",")) {
            Optional<Coin> coin = coinRepository.findByName(coinName);
            if (coin.isEmpty()) return "[ERROR] 존재하지 않는 코인입니다.\n";
            coins.add(coin.get());
        }
        List<String> codes = coins.stream().map(c -> c.getCode()).collect(Collectors.toList());
        List<String> names = coins.stream().map(c -> c.getName()).collect(Collectors.toList());

        CoinServiceGrpc.CoinServiceBlockingStub stub = CoinServiceGrpc.newBlockingStub(channel);

        GetCoinTradePriceResponse response = stub.getCoinTradePrice(GetCoinTradePriceRequest.newBuilder()
                .setCodes(codes.toString())
                .setNames(names.toString())
                .build());

        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶ 검색한 종목의 현재가 ◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀\n");
        resultBuilder.append("                                          현재가        전일 대비     거래 대금\n");
        List<Integer> tradePrices = List.of(response.getTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(p -> Integer.parseInt(p)).collect(Collectors.toList());
        List<Double> changeRates = List.of(response.getChangeRates().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(r -> Double.parseDouble(r)).collect(Collectors.toList());
        List<Double> totalTradePrices = List.of(response.getTotalTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                .stream().map(p -> Double.parseDouble(p)).collect(Collectors.toList());
        DecimalFormat df = new DecimalFormat("###,###.##");

        for (int i = 0; i < tradePrices.size(); i++) {
            CryptoInfo cryptoInfo = new CryptoInfo(names.get(i) + " (" + codes.get(i) + ") : " + df.format(tradePrices.get(i)) + " KRW  " + df.format(changeRates.get(i) * 100) + "%  "
                    + df.format(BigInteger.valueOf((long) (totalTradePrices.get(i) / 1000000))) + " 백만");
            resultBuilder.append(cryptoInfo).append("\n");
        }
        resultBuilder.append("----------------------------------------------------------------------------\n\n");
        channel.shutdown();
        return resultBuilder.toString();
    }

    public String getWatchedCoinsPrice() {
        Member member;
        try {
            member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();
            List<WatchedCoin> coins = watchedCoinRepository.findByMember(member);
            if (coins.isEmpty()) {
                return "관심 코인이 없습니다.\n";
            }
            List<String> codes = coins.stream().map(c -> c.getCode()).collect(Collectors.toList());
            List<String> names = coins.stream().map(c -> c.getName()).collect(Collectors.toList());

            CoinServiceGrpc.CoinServiceBlockingStub stub = CoinServiceGrpc.newBlockingStub(channel);

            GetCoinTradePriceResponse response = stub.getCoinTradePrice(GetCoinTradePriceRequest.newBuilder()
                    .setCodes(codes.toString())
                    .setNames(names.toString())
                    .build());

            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶▶ 관심 종목 현재가 ◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀◀\n");
            resultBuilder.append("                                          현재가        전일 대비     거래 대금\n");
            List<Integer> tradePrices = List.of(response.getTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                    .stream().map(p -> Integer.parseInt(p)).collect(Collectors.toList());
            List<Double> changeRates = List.of(response.getChangeRates().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                    .stream().map(r -> Double.parseDouble(r)).collect(Collectors.toList());
            List<Double> totalTradePrices = List.of(response.getTotalTradePrices().replace("[", "").replace("]", "").replaceAll("'", "").split(", "))
                    .stream().map(p -> Double.parseDouble(p)).collect(Collectors.toList());
            DecimalFormat df = new DecimalFormat("###,###.##");

            for (int i = 0; i < tradePrices.size(); i++) {
                CryptoInfo cryptoInfo = new CryptoInfo(names.get(i) + " (" + codes.get(i) + ") : " + df.format(tradePrices.get(i)) + " KRW  " + df.format(changeRates.get(i) * 100) + "%  "
                        + df.format(BigInteger.valueOf((long) (totalTradePrices.get(i) / 1000000))) + " 백만");
                resultBuilder.append(cryptoInfo).append("\n");
            }
            resultBuilder.append("----------------------------------------------------------------------------\n\n");
            channel.shutdown();
            return resultBuilder.toString();
        } catch (IllegalArgumentException e) {
            return e.getMessage() + "\n";
        }
    }

    @Transactional
    public String deleteWatchedCoin(String coinNames) {
        try {
            Member member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("[ERROR] 로그인이 필요합니다."));
            for (String coinName : coinNames.split(", ")) {
                WatchedCoin watchedCoin = watchedCoinRepository.findByName(coinName).orElseThrow(
                        () -> new IllegalArgumentException("[ERROR] 관심 종목에 없는 코인입니다."));
                watchedCoinRepository.delete(watchedCoin);
            }
            return coinNames + "(이/가) 관심 목록에서 삭제되었습니다.\n";
        } catch (IllegalArgumentException e) {
            return e.getMessage() + "\n";
        }
    }

}
