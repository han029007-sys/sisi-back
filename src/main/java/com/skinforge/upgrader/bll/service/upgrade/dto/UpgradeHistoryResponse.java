package com.skinforge.upgrader.bll.service.upgrade.dto;

import com.skinforge.upgrader.model.UpgradeResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpgradeHistoryResponse(
        Long id,

        String inputMarketHashName,
        String inputImageUrl,
        BigDecimal inputPrice,

        String targetMarketHashName,
        String targetImageUrl,
        BigDecimal targetPrice,

        BigDecimal chance,
        BigDecimal roll,
        UpgradeResult result,

        LocalDateTime createdAt
) {
}