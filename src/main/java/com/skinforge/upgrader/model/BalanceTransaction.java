package com.skinforge.upgrader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Entity
@Table(name = "balance_transactions")
@NoArgsConstructor
public class BalanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BalanceTransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "skin_name")
    private String skinName;

    @Column(name = "skin_image_url")
    private String skinImageUrl;

    private BalanceTransaction(
            UUID userId,
            BalanceTransactionType type,
            BigDecimal amount,
            String skinName,
            String skinImageUrl
    ) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.skinName = skinName;
        this.skinImageUrl = skinImageUrl;
        this.createdAt = LocalDateTime.now();
    }

    public static BalanceTransaction deposit(
            UUID userId,
            BigDecimal amount
    ) {
        return new BalanceTransaction(
                userId,
                BalanceTransactionType.DEPOSIT,
                amount,
                null,
                null
        );
    }

    public static BalanceTransaction skinBuy(
            UUID userId,
            BigDecimal amount,
            String skinName,
            String skinImageUrl
    ) {
        return new BalanceTransaction(
                userId,
                BalanceTransactionType.SKIN_BUY,
                amount.negate(),
                skinName,
                skinImageUrl
        );
    }

    public static BalanceTransaction skinSell(
            UUID userId,
            BigDecimal amount,
            String skinName,
            String skinImageUrl
    ) {
        return new BalanceTransaction(
                userId,
                BalanceTransactionType.SKIN_SELL,
                amount,
                skinName,
                skinImageUrl
        );
    }
}
