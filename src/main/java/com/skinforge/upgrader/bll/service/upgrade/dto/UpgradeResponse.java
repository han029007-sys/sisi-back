package com.skinforge.upgrader.bll.service.upgrade.dto;


import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.model.UpgradeResult;

import java.math.BigDecimal;

public record UpgradeResponse(
        Long upgradeId,
        SkinOfferResponse reward
) {
}