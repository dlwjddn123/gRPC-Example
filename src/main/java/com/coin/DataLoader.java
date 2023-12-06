package com.coin;

import com.coin.domain.Coin;
import com.coin.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class DataLoader implements ApplicationRunner {

    private final CoinRepository coinRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        coinRepository.save(new Coin("KRW-STX", "스택스"));
        coinRepository.save(new Coin("KRW-BTC", "비트코인"));
        coinRepository.save(new Coin("KRW-XRP", "리플"));
        coinRepository.save(new Coin("KRW-SEI", "세이"));
        coinRepository.save(new Coin("KRW-SC", "시아코인"));
        coinRepository.save(new Coin("KRW-SOL", "솔라나"));
        coinRepository.save(new Coin("KRW-IOTA", "아이오타"));
        coinRepository.save(new Coin("KRW-MLK", "밀크"));
        coinRepository.save(new Coin("KRW-FLOW", "플로우"));
        coinRepository.save(new Coin("KRW-BLUR", "블러"));
        coinRepository.save(new Coin("KRW-DOGE", "도지코인"));
        coinRepository.save(new Coin("KRW-ETH", "이더리움"));
        coinRepository.save(new Coin("KRW-EGLD", "멀티버스엑스"));
        coinRepository.save(new Coin("KRW-MASK", "마스크네트워크"));
        coinRepository.save(new Coin("KRW-BCH", "비트코인캐시"));
        coinRepository.save(new Coin("KRW-ETC", "이더리움클래식"));
        coinRepository.save(new Coin("KRW-NEAR", "니어프로토콜"));
        coinRepository.save(new Coin("KRW-T", "쓰레스홀드"));
        coinRepository.save(new Coin("KRW-ORBS", "오브스"));
        coinRepository.save(new Coin("KRW-SUI", "수이"));
        coinRepository.save(new Coin("KRW-GMT", "스테픈"));
        coinRepository.save(new Coin("KRW-MINA", "미나"));
        coinRepository.save(new Coin("KRW-MTL", "메탈"));
        coinRepository.save(new Coin("KRW-SAND", "샌드박스"));
        coinRepository.save(new Coin("KRW-BTG", "비트코인골드"));
        coinRepository.save(new Coin("KRW-ELF", "엘프"));
        coinRepository.save(new Coin("KRW-AXS", "엑시인피니티"));
        coinRepository.save(new Coin("KRW-BAT", "베이직어텐션토큰"));
    }
}
