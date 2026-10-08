package com.skinforge.upgrader.controller.request;

public record UpgradeRequest(
        Long inputInventoryId,
        String targetListingId
) {}
