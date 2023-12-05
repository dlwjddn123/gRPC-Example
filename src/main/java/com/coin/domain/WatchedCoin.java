package com.coin.domain;

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
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public WatchedCoin(String name, Member member) {
        this.name = name;
        this.member = member;
    }
}
