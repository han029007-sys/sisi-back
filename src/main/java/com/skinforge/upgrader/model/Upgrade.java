package com.skinforge.upgrader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Entity
@Table(name = "upgrades")
@NoArgsConstructor
public class Upgrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "input_inventory_id", nullable = false)
    private Long inputInventoryId;

    @Column(name = "output_inventory_id")
    private Long outputInventoryId;

    @Column(name = "input_listing_id")
    private String inputListingId;

    @Column(name = "input_market_hash_name", nullable = false)
    private String inputMarketHashName;

    @Column(name = "input_image_url")
    private String inputImageUrl;

    @Column(name = "input_price", nullable = false)
    private BigDecimal inputPrice;

    @Column(name = "target_listing_id")
    private String targetListingId;

    @Column(name = "target_market_hash_name", nullable = false)
    private String targetMarketHashName;

    @Column(name = "target_image_url")
    private String targetImageUrl;

    @Column(name = "target_price", nullable = false)
    private BigDecimal targetPrice;

    @Column(nullable = false)
    private BigDecimal chance;

    @Column(nullable = false)
    private BigDecimal roll;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UpgradeResult result;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Upgrade(
            UUID userId,
            Long inputInventoryId,
            Long outputInventoryId,
            String inputListingId,
            String inputMarketHashName,
            String inputImageUrl,
            BigDecimal inputPrice,
            String targetListingId,
            String targetMarketHashName,
            String targetImageUrl,
            BigDecimal targetPrice,
            BigDecimal chance,
            BigDecimal roll,
            UpgradeResult result
    ) {
        this.userId = userId;
        this.inputInventoryId = inputInventoryId;
        this.outputInventoryId = outputInventoryId;
        this.inputListingId = inputListingId;
        this.inputMarketHashName = inputMarketHashName;
        this.inputImageUrl = inputImageUrl;
        this.inputPrice = inputPrice;
        this.targetListingId = targetListingId;
        this.targetMarketHashName = targetMarketHashName;
        this.targetImageUrl = targetImageUrl;
        this.targetPrice = targetPrice;
        this.chance = chance;
        this.roll = roll;
        this.result = result;
        this.createdAt = LocalDateTime.now();
    }
}