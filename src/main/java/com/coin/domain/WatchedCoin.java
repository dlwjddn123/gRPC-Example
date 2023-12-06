package com.coin.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class WatchedCoin {

    @Id @GeneratedValue
    @Column(name = "watched_coin_id")
    private Long id;
    private String code;
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public WatchedCoin(String code, String name, Member member) {
        this.code = code;
        this.name = name;
        this.member = member;
    }
}
