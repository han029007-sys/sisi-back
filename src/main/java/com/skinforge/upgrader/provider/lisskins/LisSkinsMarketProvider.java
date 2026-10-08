package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.bll.service.skin.dto.PriceSort;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.bll.service.skin.dto.SkinPageResponse;
import com.skinforge.upgrader.metadata.SkinMetadataProvider;
import com.skinforge.upgrader.provider.SkinMarketProvider;
import com.skinforge.upgrader.provider.lisskins.dto.LisSkinDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LisSkinsMarketProvider implements SkinMarketProvider {

    private final LisSkinRedisRepository redisRepository;
    private final SkinMetadataProvider metadataProvider;

    @Override
    public SkinPageResponse findByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int limit,
            PriceSort priceSort,
            String cursor
    ) {
        int offset = cursor == null
                ? 0
                : Integer.parseInt(cursor);

        var skins = redisRepository.search(
                minPrice,
                maxPrice,
                priceSort,
                offset,
                limit
        );

        var items = skins.stream()
                .map(this::map)
                .toList();

        String nextCursor = items.size() == limit
                ? String.valueOf(offset + limit)
                : null;

        return new SkinPageResponse(
                items,
                nextCursor,
                nextCursor != null
        );
    }

    @Override
    public SkinOfferResponse findByListingId(String listingId) {
        var skin = redisRepository.findByListingId(
                Long.parseLong(listingId)
        );

        if (skin == null) {
            throw new IllegalArgumentException(
                    "LIS skin not found: " + listingId
            );
        }

        return map(skin);
    }

    private SkinOfferResponse map(LisSkinDocument skin) {
        var metadata = metadataProvider.get(skin.name());

        String rarity = metadata != null
                ? metadata.rarity()
                : skin.rarity();

        String wear = metadata != null && metadata.wear() != null
                ? metadata.wear()
                : skin.wear();

        return new SkinOfferResponse(
                String.valueOf(skin.id()),
                skin.name(),
                buildImageUrl(skin.itemClassId()),
                skin.price(),
                rarity,
                wear
        );
    }

    private String buildImageUrl(String classId) {
        if (classId == null) {
            return null;
        }

        return "https://steamcommunity.com/economy/image/class/730/"
                + classId;
    }
}
