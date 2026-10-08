package com.skinforge.upgrader.model;

import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "inventories")
@NoArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "listing_id")
    private String listingId;

    @Column(name = "market_hash_name", nullable = false)
    private String marketHashName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private BigDecimal price;

    private String rarity;

    private String wear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public InventoryItem(
            UUID userId,
            String listingId,
            String marketHashName,
            String imageUrl,
            BigDecimal price,
            String rarity,
            String wear
    ) {
        this.userId = userId;
        this.listingId = listingId;
        this.marketHashName = marketHashName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rarity = rarity;
        this.wear = wear;
        this.status = InventoryStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public void consume() {
        this.status = InventoryStatus.CONSUMED;
    }

    public void markWithdrawPending() {
        this.status = InventoryStatus.WITHDRAW_PENDING;
    }

    public void markWithdrawn() {
        this.status = InventoryStatus.WITHDRAWN;
    }

    public void markAvailable() {
        this.status = InventoryStatus.AVAILABLE;
    }

    public void sell() {
        this.status = InventoryStatus.SOLD;
    }
}