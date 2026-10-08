package com.skinforge.upgrader.provider.lisskins.dto;

import java.math.BigDecimal;

public record LisSkinDocument(
        Long id,
        String name,
        BigDecimal price,
        Double floatValue,
        String rarity,
        String wear,
        String weapon,
        Integer deliveryType,
        String itemClassId
) {
}
