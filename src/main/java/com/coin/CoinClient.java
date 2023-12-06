package com.coin;

import com.coin.domain.Coin;
import com.coin.repository.CoinRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CoinClient {

    private final CoinRepository coinRepository;

    @GetMapping("/coin/trade-price")
    public void test() {
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

        System.out.println("▶▶▶▶▶▶▶▶▶▶ 현재가 ◀◀◀◀◀◀◀◀◀◀ \n");
        List<String> coinPrices = List.of(response.getResult().replace("[", "").replace("]", "").replaceAll("'", "").split(", "));
        for (String coinPrice : coinPrices) {
            System.out.println(coinPrice);
        }

        channel.shutdown();
    }
}
