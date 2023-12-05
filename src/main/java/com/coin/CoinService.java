package com.coin;


import com.coin.domain.Member;
import com.coin.repository.MemberRepository;
import com.coin.repository.PurchasedCoinRepository;
import com.coin.repository.WatchedCoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CoinService {

    private final MemberRepository memberRepository;
    private final PurchasedCoinRepository purchasedCoinRepository;
    private final WatchedCoinRepository watchedCoinRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public void join(String loginId, String nickname, String accountName, String password) {
        Member member = Member.builder()
                .loginId(loginId)
                .nickname(nickname)
                .account(Member.Account.of(accountName))
                .password(bCryptPasswordEncoder.encode(password))
                .build();

        memberRepository.save(member);
    }
}
