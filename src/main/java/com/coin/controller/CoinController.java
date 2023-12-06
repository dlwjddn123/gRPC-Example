package com.coin.controller;

import com.coin.service.CoinService;
import com.coin.service.CoinTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;
    private final CoinTradeService coinTradeService;

    @GetMapping("/coin/trade-price")
    public void getAllCoinsTradePrice() {
        coinService.getAllCoinsTradePrice();
    }

    @GetMapping("/coin/watched-coins")
    public void getWatchedCoinsPrice() {
        coinService.getWatchedCoinsPrice();
    }

    @PostMapping("/coin/watched-coins")
    public void addWatchedCoin(@RequestParam String coinNames) {
        coinService.addWatchedCoin(coinNames);
    }

    @DeleteMapping("/coin/watched-coins")
    public void deleteWatchedCoin(@RequestParam String coinNames) {
        coinService.deleteWatchedCoin(coinNames);
    }

    @PostMapping("/coin/purchase")
    public void purchaseCoin(@RequestParam String coinName, @RequestParam int count) {
        coinTradeService.purchaseCoin(coinName, count);
    }

    @PostMapping("/coin/sell")
    public void sellCoin(@RequestParam String coinName, @RequestParam int count) {
        coinTradeService.sellCoin(coinName, count);
    }

    @GetMapping("/holdings")
    public void getHoldings() {
        coinTradeService.getHoldings();
    }

    @GetMapping("/trade-history")
    public void getTradeHistory() {
        coinTradeService.getTradeHistory();
    }

}
