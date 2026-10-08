package com.skinforge.upgrader.provider.lisskins.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LisMarketSnapshotResponse(
        @JsonProperty("last_update")
        Long lastUpdate,

        String status,

        List<LisSkinSnapshotItem> items
) {
}