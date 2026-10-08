package com.skinforge.upgrader.bll.service.upgrade.dto;

import java.math.BigDecimal;

public record BestWinResponse(
        String marketHashName,
        String imageUrl,
        BigDecimal price
) {
}
