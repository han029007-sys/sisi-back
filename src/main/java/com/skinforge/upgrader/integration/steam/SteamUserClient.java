package com.skinforge.upgrader.integration.steam;

import com.skinforge.upgrader.integration.steam.dto.SteamPlayer;
import com.skinforge.upgrader.integration.steam.dto.SteamPlayerSummariesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SteamUserClient {

    private final RestClient restClient;
    private final String apiKey;

    public SteamUserClient(
            @Value("${steam.api.key}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.steampowered.com")
                .build();
    }

    public SteamPlayer getPlayer(String steamId) {
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ISteamUser/GetPlayerSummaries/v2/")
                        .queryParam("key", apiKey)
                        .queryParam("steamids", steamId)
                        .build())
                .retrieve()
                .body(SteamPlayerSummariesResponse.class);

        if (response == null
                || response.response() == null
                || response.response().players() == null
                || response.response().players().isEmpty()) {
            throw new IllegalStateException("Steam user not found: " + steamId);
        }

        return response.response().players().getFirst();
    }
}
