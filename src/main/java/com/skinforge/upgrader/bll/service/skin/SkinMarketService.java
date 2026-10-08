package com.skinforge.upgrader.bll.service.skin;

import com.skinforge.upgrader.bll.service.balance.CurrencyPriceService;
import com.skinforge.upgrader.bll.service.skin.dto.PriceSort;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.bll.service.skin.dto.SkinPageResponse;
import com.skinforge.upgrader.provider.SkinMarketProvider;
import com.skinforge.upgrader.provider.lisskins.LisSkinsMarketProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
public class SkinMarketService {

    private final SkinMarketProvider skinMarketProvider;
    private final CurrencyPriceService currencyPriceService;

    public SkinMarketService(LisSkinsMarketProvider marketProvider, CurrencyPriceService currencyPriceService) {
        this.skinMarketProvider = marketProvider;
        this.currencyPriceService = currencyPriceService;
    }

    public SkinPageResponse find(BigDecimal minPriceRub,
            BigDecimal maxPriceRub, int limit,
            PriceSort priceSort, String cursor) {
        BigDecimal minPriceUsd = currencyPriceService.skinRubToUsd(minPriceRub);

        BigDecimal maxPriceUsd = currencyPriceService.skinRubToUsd(maxPriceRub);

        var page = skinMarketProvider.findByPriceRange(
                minPriceUsd,
                maxPriceUsd,
                limit,
                priceSort,
                cursor
        );

        Comparator<SkinOfferResponse> comparator = Comparator.comparing(SkinOfferResponse::price);

        if (priceSort == PriceSort.DESC) {
            comparator = comparator.reversed();
        }

        var itemsRub = page.items().stream()
                .map(skin -> new SkinOfferResponse(
                        skin.listingId(),
                        skin.marketHashName(),
                        skin.imageUrl(),
                        currencyPriceService.skinUsdToRub(skin.price()),
                        skin.rarity(),
                        skin.wear()
                ))
                .sorted(comparator)
                .toList();

        return new SkinPageResponse(
                itemsRub,
                page.nextCursor(),
                page.hasMore()
        );
    }

    public SkinOfferResponse findByListingId(String listingId) {
        var skin = skinMarketProvider.findByListingId(listingId);

        return new SkinOfferResponse(
                skin.listingId(),
                skin.marketHashName(),
                skin.imageUrl(),
                currencyPriceService.skinUsdToRub(skin.price()),
                skin.rarity(),
                skin.wear()
        );
    }
}
