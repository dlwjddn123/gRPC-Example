package com.coin.service;


import com.coin.CoinServiceGrpc;
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

import java.util.List;
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
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            for (String coinName : coinNames.split(", ")) {
                Coin coin = coinRepository.findByName(coinName).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 코인입니다."));
                watchedCoinRepository.save(new WatchedCoin(coin.getCode(), coin.getName(), member));
            }
            return coinNames + "이/가 관심 목록에 추가되었습니다.";
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
        resultBuilder.append("▶▶▶▶▶▶▶▶▶▶ 현재가 ◀◀◀◀◀◀◀◀◀◀ \n\n");
//        System.out.println("▶▶▶▶▶▶▶▶▶▶ 현재가 ◀◀◀◀◀◀◀◀◀◀ \n");
        List<String> coinPrices = List.of(response.getResult().replace("[", "").replace("]", "").replaceAll("'", "").split(", "));
        for (String coinPrice : coinPrices) {
//            System.out.println(coinPrice);
            resultBuilder.append(coinPrice).append("\n");
        }
        resultBuilder.append("-----------------------------------\n\n");
//        System.out.println("-----------------------------------\n");
        channel.shutdown();
        System.out.println(resultBuilder.toString());
        return resultBuilder.toString();
    }

    public String getWatchedCoinsPrice() {
        Member member;
        try {
            member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId()).orElseThrow(
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();
            List<WatchedCoin> coins = watchedCoinRepository.findByMember(member);
            List<String> codes = coins.stream().map(c -> c.getCode()).collect(Collectors.toList());
            List<String> names = coins.stream().map(c -> c.getName()).collect(Collectors.toList());

            CoinServiceGrpc.CoinServiceBlockingStub stub = CoinServiceGrpc.newBlockingStub(channel);

            GetCoinTradePriceResponse response = stub.getCoinTradePrice(GetCoinTradePriceRequest.newBuilder()
                    .setCodes(codes.toString())
                    .setNames(names.toString())
                    .build());

            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append("▶▶▶▶▶▶▶▶▶▶ 관심 종목 현재가 ◀◀◀◀◀◀◀◀◀◀ \n\n");
            List<String> coinPrices = List.of(response.getResult().replace("[", "").replace("]", "").replaceAll("'", "").split(", "));
            for (String coinPrice : coinPrices) {
                resultBuilder.append(coinPrice).append("\n");
            }
            resultBuilder.append("-----------------------------------\n");
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
                    () -> new IllegalArgumentException("로그인이 필요합니다."));
            for (String coinName : coinNames.split(", ")) {
                WatchedCoin watchedCoin = watchedCoinRepository.findByName(coinName).orElseThrow(
                        () -> new IllegalArgumentException("관심 종목에 없는 코인입니다."));
                watchedCoinRepository.delete(watchedCoin);
            }
            return coinNames + "이/가 관심 목록에서 삭제되었습니다.\n";
        } catch (IllegalArgumentException e) {
            return e.getMessage() + "\n";
        }
    }

}
