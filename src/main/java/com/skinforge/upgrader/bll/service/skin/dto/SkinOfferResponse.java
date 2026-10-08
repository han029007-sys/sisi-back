package com.skinforge.upgrader.bll.service.skin.dto;

import java.math.BigDecimal;

public record SkinOfferResponse(
        String listingId,
        String marketHashName,
        String imageUrl,
        BigDecimal price,
        String rarity,
        String wear
) {
}
