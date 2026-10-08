package com.skinforge.upgrader.provider.lisskins;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Component
public class LisMarketSearchClient {

    private final RestClient restClient;

    public LisMarketSearchClient(
            @Value("${lisskins.api.key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.lis-skins.com")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + apiKey
                )
                .build();
    }

    public LisMarketItem findCheapestUnlocked(
            String name
    ) {

        LisMarketSearchResponse response = restClient.get()
                .uri(builder ->
                        builder
                                .path("/v1/market/search")
                                .queryParam("game", "csgo")
                                .queryParam("names[]", name)
                                .queryParam("only_unlocked", 1)
                                .queryParam("sort_by", "lowest_price")
                                .build()
                )
                .retrieve()
                .body(LisMarketSearchResponse.class);

        if (response == null
                || response.data() == null
                || response.data().isEmpty()) {
            return null;
        }

        return response.data()
                .stream()
                .filter(item -> name.equals(item.name()))
                .min(
                        Comparator.comparing(
                                LisMarketItem::price
                        )
                )
                .orElse(null);
    }

    public record LisMarketSearchResponse(
            List<LisMarketItem> data
    ) {
    }

    public record LisMarketItem(
            Long id,
            String name,
            BigDecimal price,
            String unlock_at,
            String item_class_id,
            String item_float,
            Integer delivery_type
    ) {
    }
}