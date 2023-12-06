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
    public String getAllCoinsTradePrice() {
        return coinService.getAllCoinsTradePrice();
    }

    @GetMapping("/coin/watched-coins")
    public String getWatchedCoinsPrice() {
        return coinService.getWatchedCoinsPrice();
    }

    @PostMapping("/coin/watched-coins")
    public String addWatchedCoin(@RequestParam String coinNames) {
        return coinService.addWatchedCoin(coinNames);
    }

    @DeleteMapping("/coin/watched-coins")
    public String deleteWatchedCoin(@RequestParam String coinNames) {
        return coinService.deleteWatchedCoin(coinNames);
    }

    @PostMapping("/coin/purchase")
    public String purchaseCoin(@RequestParam String coinName, @RequestParam int count) {
        return coinTradeService.purchaseCoin(coinName, count);
    }

    @PostMapping("/coin/sell")
    public String sellCoin(@RequestParam String coinName, @RequestParam int count) {
        return coinTradeService.sellCoin(coinName, count);
    }

    @GetMapping("/holdings")
    public String getHoldings() {
        return coinTradeService.getHoldings();
    }

    @GetMapping("/trade-history")
    public String getTradeHistory() {
        return coinTradeService.getTradeHistory();
    }

}
