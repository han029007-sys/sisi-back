package com.skinforge.upgrader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "withdrawals")
@NoArgsConstructor
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "project_id", nullable = false, unique = true)
    private String projectId;

    @Column(name = "market_hash_name", nullable = false)
    private String marketHashName;

    @Column(name = "provider_listing_id")
    private String providerListingId;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Withdrawal(UUID userId, Long inventoryId, String projectId, String marketHashName, String providerListingId, BigDecimal price) {
        this.userId = userId;
        this.inventoryId = inventoryId;
        this.projectId = projectId;
        this.marketHashName = marketHashName;
        this.providerListingId = providerListingId;
        this.price = price;
        this.status = WithdrawalStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markProcessing() {
        this.status = WithdrawalStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markSuccess() {
        this.status = WithdrawalStatus.SUCCESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = WithdrawalStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void setProviderData(String providerListingId, BigDecimal price) {

        this.providerListingId = providerListingId;

        this.price = price;

        this.updatedAt = LocalDateTime.now();
    }

}