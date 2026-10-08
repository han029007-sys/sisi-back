package com.skinforge.upgrader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "steam_id", nullable = false, unique = true)
    private String steamId;

    private String username;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "first_deposit_completed", nullable = false)
    private boolean firstDepositCompleted;

    @Column(name = "trade_url")
    private String tradeUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public User(String steamId, String username, String avatarUrl){
        this.steamId = steamId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        balance = BigDecimal.ZERO;
        createdAt = LocalDateTime.now();
        firstDepositCompleted = false;
    }

    public void deposit(BigDecimal amount){
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount){
        balance = balance.subtract(amount);
    }

    public void setFirstDepositCompleted(){
        firstDepositCompleted = true;
    }

    public void updateTradeUrl(String tradeUrl) {
        this.tradeUrl = tradeUrl;
    }

    public void updateProfile(
            String username,
            String avatarUrl
    ) {
        this.username = username;
        this.avatarUrl = avatarUrl;
    }
}