package com.skinforge.upgrader.provider.lisskins.ws;

import com.skinforge.upgrader.metadata.SkinMetadataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class LisMarketEventHandler {

    private final SkinMetadataProvider metadataProvider;
    private final LisSkinPriceUpdater priceUpdater;

    public void handle(JsonNode push) {

        JsonNode pub =
                push.get("pub");

        if (pub == null) {
            return;
        }

        JsonNode data =
                pub.get("data");

        if (data == null) {
            return;
        }

        String name =
                data.path("name").asText();

        if (!isCs2(name)) {
            return;
        }

        String event =
                data.path("event").asText();

        switch (event) {

            case "obtained_skin_added" ->
                    priceUpdater.onAdded(
                            data
                    );

            case "obtained_skin_deleted" ->
                    priceUpdater.onDeleted(
                            data.path("id").asLong(),
                            name
                    );

            case "obtained_skin_price_changed" ->
                    priceUpdater.onPriceChanged(
                            data
                    );

            default ->
                    log.debug(
                            "Unknown LIS event: {}",
                            event
                    );
        }
    }

    private boolean isCs2(String name) {

        return name != null
                && !name.isBlank()
                && metadataProvider.get(name) != null;
    }
}