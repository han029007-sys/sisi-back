package com.skinforge.upgrader.provider.lisskins.dto;

import java.math.BigDecimal;
import java.util.List;

public record LisBuyRequest(
        List<Long> ids,
        String partner,
        String token,
        BigDecimal max_price,
        String custom_id,
        Boolean skip_unavailable
) {
}