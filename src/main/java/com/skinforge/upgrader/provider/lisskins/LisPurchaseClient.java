package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.provider.lisskins.dto.LisBuyRequest;
import com.skinforge.upgrader.provider.lisskins.dto.LisBuyResponse;
import com.skinforge.upgrader.provider.lisskins.dto.LisPurchaseInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
public class LisPurchaseClient {

    private final RestClient restClient;

    public LisPurchaseClient(
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

    public LisBuyResponse buy(
            Long listingId,
            String partner,
            String token,
            BigDecimal maxPrice,
            String customId
    ) {

        LisBuyRequest request =
                new LisBuyRequest(
                        List.of(listingId),
                        partner,
                        token,
                        maxPrice,
                        customId,
                        false
                );

        LisBuyResponse response = restClient.post()
                .uri("/v1/market/buy")
                .body(request)
                .retrieve()
                .body(LisBuyResponse.class);

        if (response == null
                || response.data() == null) {
            throw new IllegalStateException(
                    "Empty LIS buy response"
            );
        }

        return response;
    }

    public LisPurchaseInfoResponse getByCustomId(
            String customId
    ) {

        LisPurchaseInfoResponse response = restClient.get()
                .uri(builder ->
                        builder
                                .path("/v1/market/info")
                                .queryParam(
                                        "custom_ids[]",
                                        customId
                                )
                                .build()
                )
                .retrieve()
                .body(
                        LisPurchaseInfoResponse.class
                );

        if (response == null) {
            throw new IllegalStateException(
                    "Empty LIS market/info response"
            );
        }

        return response;
    }
}