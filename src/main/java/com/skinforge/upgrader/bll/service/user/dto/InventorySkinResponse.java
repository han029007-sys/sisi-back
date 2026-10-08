package com.skinforge.upgrader.bll.service.user.dto;

import java.math.BigDecimal;

public record InventorySkinResponse(
        Long inventoryId,
        String listingId,
        String marketHashName,
        String imageUrl,
        BigDecimal price,
        String rarity,
        String wear
) {
}
