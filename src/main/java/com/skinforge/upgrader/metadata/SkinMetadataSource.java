package com.skinforge.upgrader.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SkinMetadataSource(
        @JsonProperty("market_hash_name")
        String marketHashName,

        Rarity rarity,

        Wear wear
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rarity(
            String name,
            String color
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wear(
            String name
    ) {}
}