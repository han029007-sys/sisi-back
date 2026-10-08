package com.skinforge.upgrader.provider.lisskins.dto;

import java.math.BigDecimal;

public record LisSticker(
        String name,
        String name_full,
        String image,
        BigDecimal wear,
        Integer slot
) {
}