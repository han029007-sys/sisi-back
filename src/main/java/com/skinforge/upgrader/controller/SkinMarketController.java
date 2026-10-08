package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.skin.SkinMarketService;
import com.skinforge.upgrader.bll.service.skin.SkinPurchaseService;
import com.skinforge.upgrader.bll.service.skin.dto.PriceSort;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.bll.service.skin.dto.SkinPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/skins")
@RequiredArgsConstructor
public class SkinMarketController {

    private final SkinMarketService skinMarketService;
    private final SkinPurchaseService skinPurchaseService;

    @GetMapping
    public SkinPageResponse getSkins(
            @RequestParam(defaultValue = "0") BigDecimal minPrice,
            @RequestParam(defaultValue = "10000000") BigDecimal maxPrice,
            @RequestParam(defaultValue = "ASC") PriceSort priceSort,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return skinMarketService.find(minPrice, maxPrice, limit, priceSort, cursor);
    }

    @PostMapping("/{listingId}/buy")
    public SkinOfferResponse buy(
            @PathVariable String listingId,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        return skinPurchaseService.buy(userId, listingId);
    }
}