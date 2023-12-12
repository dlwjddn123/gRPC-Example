package com.coin.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Member {

    public enum Account {
        TOSS("토스"), KAKAO("카카오"), KBANK("케이뱅크");

        private String name;

        Account(String name) {
            this.name = name;
        }

        public static Account of(String name) {
            return Arrays.stream(Account.values())
                    .filter(v -> v.name.equals(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("[ERROR] 토스, 카카오, 케이뱅크로만 계좌 등록이 가능합니다."));
        }
    }

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String loginId;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Account account;
    private String password;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WatchedCoin> watchedCoins = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PurchasedCoin> purchasedCoins = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TradeHistory> tradeHistories = new ArrayList<>();

    @Builder
    public Member(String loginId, String nickname, Account account, String password) {
        this.loginId = loginId;
        this.nickname = nickname;
        this.account = account;
        this.password = password;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public String modify(String nickname, String accountName) {
        this.nickname = nickname;
        try {
            Account.of(accountName);
            this.account = Account.of(accountName);
            return "회원 정보가 수정되었습니다.";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
