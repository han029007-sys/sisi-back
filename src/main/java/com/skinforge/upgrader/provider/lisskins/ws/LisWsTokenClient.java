package com.skinforge.upgrader.provider.lisskins.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LisWsTokenClient {

    private final RestClient restClient;

    public LisWsTokenClient(
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

    public String getToken() {

        LisWsTokenResponse response = restClient.get()
                .uri("/v1/user/get-ws-token")
                .retrieve()
                .body(LisWsTokenResponse.class);

        if (response == null
                || response.data() == null
                || response.data().token() == null) {

            throw new IllegalStateException(
                    "LIS websocket token not received"
            );
        }

        return response.data().token();
    }
}