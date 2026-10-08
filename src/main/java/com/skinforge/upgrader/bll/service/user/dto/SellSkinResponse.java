package com.skinforge.upgrader.bll.service.user.dto;

import java.math.BigDecimal;

public record SellSkinResponse(
        Long inventoryId,
        BigDecimal soldPrice,
        BigDecimal balance
) {}
