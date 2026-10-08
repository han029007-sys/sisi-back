package com.skinforge.upgrader.provider.lisskins.dto;

import java.math.BigDecimal;
import java.util.List;

public record LisPurchaseInfoResponse(
        List<Purchase> data
) {

    public record Purchase(
            Long purchase_id,
            String steam_id,
            String created_at,
            String custom_id,
            List<Skin> skins
    ) {
    }

    public record Skin(
            Long id,
            String name,
            BigDecimal price,
            String status,
            String return_reason,
            BigDecimal return_charged_commission,
            String error,
            String steam_trade_offer_id,
            String steam_trade_offer_created_at,
            String steam_trade_offer_expiry_at,
            String steam_trade_offer_finished_at
    ) {
    }
}