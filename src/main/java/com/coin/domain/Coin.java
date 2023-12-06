package com.coin.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class Coin {

    @Id @GeneratedValue
    @Column(name = "coin_id")
    private Long id;
    private String code;
    private String name;

    public Coin(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
