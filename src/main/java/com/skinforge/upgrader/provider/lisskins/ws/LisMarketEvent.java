package com.skinforge.upgrader.provider.lisskins.ws;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record LisMarketEvent(
        Long id,
        String name,
        BigDecimal price,

        @JsonProperty("name_tag")
        String nameTag,

        @JsonProperty("unlock_at")
        String unlockAt,

        @JsonProperty("created_at")
        String createdAt,

        @JsonProperty("item_float")
        String itemFloat,

        @JsonProperty("item_class_id")
        String itemClassId,

        @JsonProperty("item_paint_seed")
        Integer itemPaintSeed,

        @JsonProperty("item_paint_index")
        Integer itemPaintIndex,

        String event
) {
}
