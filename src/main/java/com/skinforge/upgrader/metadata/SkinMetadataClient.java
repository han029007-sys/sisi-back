package com.skinforge.upgrader.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SkinMetadataClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://raw.githubusercontent.com")
            .build();

    public List<SkinMetadataSource> getSkins() {
        String json = restClient.get()
                .uri("/ByMykel/CSGO-API/main/public/api/en/skins_not_grouped.json")
                .retrieve()
                .body(String.class);

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<SkinMetadataSource>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse skin metadata", e);
        }
    }
}