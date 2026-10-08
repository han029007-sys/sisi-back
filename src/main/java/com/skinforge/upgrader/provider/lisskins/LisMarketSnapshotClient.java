package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.provider.lisskins.dto.LisMarketSnapshotResponse;
import com.skinforge.upgrader.provider.lisskins.dto.LisSkinSnapshotItem;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class LisMarketSnapshotClient {

    private static final String URL =
            "https://lis-skins.com/market_export_json/api_csgo_full.json";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public LisMarketSnapshotClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public List<LisSkinSnapshotItem> loadAll() {
        String json = restClient.get()
                .uri(URL)
                .retrieve()
                .body(String.class);

        try {
            LisMarketSnapshotResponse response =
                    objectMapper.readValue(
                            json,
                            LisMarketSnapshotResponse.class
                    );

            return response.items();

        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Failed to parse LIS snapshot",
                    e
            );
        }
    }
}