package com.skinforge.upgrader.metadata;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkinMetadataProvider {

    private final SkinMetadataClient client;

    private final Map<String, SkinMetadata> metadata = new HashMap<>();

    @PostConstruct
    public void load() {
        var skins = client.getSkins();

        for (var skin : skins) {
            if (skin.marketHashName() == null) {
                continue;
            }

            metadata.put(
                    skin.marketHashName(),
                    new SkinMetadata(
                            skin.rarity() != null ? skin.rarity().name() : null,
                            skin.rarity() != null ? skin.rarity().color() : null,
                            skin.wear() != null ? skin.wear().name() : null
                    )
            );
        }

        log.info("Loaded {} skin metadata entries", metadata.size());
    }

    public SkinMetadata get(String marketHashName) {
        SkinMetadata metadata = this.metadata.get(marketHashName);

        if (metadata != null) {
            return metadata;
        }

        return findFallback(marketHashName);
    }
    private SkinMetadata findFallback(String marketHashName) {
        String normalized = marketHashName
                .replace(" Doppler Ruby", " Doppler")
                .replace(" Doppler Sapphire", " Doppler")
                .replace(" Doppler Black Pearl", " Doppler")
                .replace(" Gamma Doppler Emerald", " Gamma Doppler")
                .replace(" Gamma Doppler Phase 1", " Gamma Doppler")
                .replace(" Gamma Doppler Phase 2", " Gamma Doppler")
                .replace(" Gamma Doppler Phase 3", " Gamma Doppler")
                .replace(" Gamma Doppler Phase 4", " Gamma Doppler");

        return metadata.get(normalized);
    }

}