package com.skinforge.upgrader.provider;

import com.skinforge.upgrader.bll.service.skin.dto.PriceSort;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.bll.service.skin.dto.SkinPageResponse;

import java.math.BigDecimal;
import java.util.List;

public interface SkinMarketProvider {

    SkinPageResponse findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int limit, PriceSort priceSort, String cursor);

    SkinOfferResponse findByListingId(String listingId);
}
