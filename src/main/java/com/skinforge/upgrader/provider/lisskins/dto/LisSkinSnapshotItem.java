package com.skinforge.upgrader.provider.lisskins.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record LisSkinSnapshotItem(
        Long id,
        String name,
        BigDecimal price,

        @JsonProperty("unlock_at")
        String unlockAt,

        @JsonProperty("item_class_id")
        String itemClassId,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("item_asset_id")
        String itemAssetId,

        @JsonProperty("game_id")
        Integer gameId,

        @JsonProperty("is_internal")
        Boolean internal,

        @JsonProperty("delivery_type")
        Integer deliveryType,

        @JsonProperty("item_link")
        String itemLink,

        @JsonProperty("item_float")
        String itemFloat,

        @JsonProperty("name_tag")
        String nameTag,

        @JsonProperty("item_paint_index")
        Integer itemPaintIndex,

        @JsonProperty("item_paint_seed")
        Integer itemPaintSeed,

        List<LisSticker> stickers
) {
}
