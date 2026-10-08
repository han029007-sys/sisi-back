package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.integration.steam.SteamTradeUrlParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LisPurchaseService {

    private final LisPurchaseClient purchaseClient;
    private final LisMarketSearchClient marketClient;
    private final SteamTradeUrlParser tradeUrlParser;

    public LisPurchaseResult buy(
            String tradeUrl,
            String marketHashName,
            String customId
    ) {

        var tradeData =
                tradeUrlParser.parse(tradeUrl);

        var listing =
                marketClient.findCheapestUnlocked(
                        marketHashName
                );

        if (listing == null) {
            throw new IllegalStateException(
                    "Skin is not available on LIS: "
                            + marketHashName
            );
        }

        var response =
                purchaseClient.buy(
                        listing.id(),
                        tradeData.partner(),
                        tradeData.token(),
                        listing.price(),
                        customId
                );

        if (response.data().skins() == null
                || response.data().skins().isEmpty()) {

            throw new IllegalStateException(
                    "LIS returned purchase without skins"
            );
        }

        var purchased =
                response.data()
                        .skins()
                        .getFirst();

        return new LisPurchaseResult(
                response.data().purchase_id(),
                purchased.id(),
                purchased.price(),
                purchased.status()
        );
    }

    public LisPurchaseStatus getStatus(
            String customId
    ) {

        var response =
                purchaseClient.getByCustomId(
                        customId
                );

        if (response.data() == null
                || response.data().isEmpty()) {

            throw new IllegalStateException(
                    "LIS purchase not found: "
                            + customId
            );
        }

        var purchase =
                response.data().getFirst();

        if (purchase.skins() == null
                || purchase.skins().isEmpty()) {

            throw new IllegalStateException(
                    "LIS purchase has no skins: "
                            + customId
            );
        }

        var skin =
                purchase.skins().getFirst();

        return new LisPurchaseStatus(
                purchase.purchase_id(),
                skin.id(),
                skin.status(),
                skin.return_reason(),
                skin.error()
        );
    }

    public record LisPurchaseResult(
            Long purchaseId,
            Long listingId,
            BigDecimal price,
            String status
    ) {
    }

    public record LisPurchaseStatus(
            Long purchaseId,
            Long listingId,
            String status,
            String returnReason,
            String error
    ) {
    }
}