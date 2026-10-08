
package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.metadata.SkinMetadataProvider;
import com.skinforge.upgrader.provider.lisskins.dto.LisSkinDocument;
import com.skinforge.upgrader.provider.lisskins.dto.LisSkinSnapshotItem;
import com.skinforge.upgrader.provider.lisskins.ws.LisWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@Slf4j
public class LisMarketInitializer {

    private final LisMarketSnapshotClient snapshotClient;
    private final LisSkinRedisRepository redisRepository;
    private final SkinMetadataProvider metadataProvider;
    private final LisWebSocketClient webSocketClient;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        try {

            if (!redisRepository.isEmpty()) {

                log.info(
                        "LIS Redis catalog already exists, skipping snapshot"
                );

                webSocketClient.connect();
                return;
            }

            log.info("Loading LIS market snapshot (streaming)...");

            Map<String, LisSkinSnapshotItem> cheapestByName =
                    new HashMap<>();

            AtomicLong processed = new AtomicLong();
            AtomicLong skippedWithoutImage = new AtomicLong();
            AtomicLong skippedNotCs2 = new AtomicLong();

            snapshotClient.forEachSkin(skin -> {

                long count = processed.incrementAndGet();

                if (!hasImage(skin)) {
                    skippedWithoutImage.incrementAndGet();
                    return;
                }

                if (skin.name() == null || skin.name().isBlank()) {
                    return;
                }

                if (skin.price() == null) {
                    return;
                }

                if (metadataProvider.get(skin.name()) == null) {
                    skippedNotCs2.incrementAndGet();
                    return;
                }

                cheapestByName.merge(
                        skin.name(),
                        skin,
                        this::cheapest
                );

                if (count % 100_000 == 0) {
                    log.info(
                            "LIS snapshot: processed {}, unique {}",
                            count,
                            cheapestByName.size()
                    );
                }
            });

            log.info(
                    "LIS snapshot processed: {} listings",
                    processed.get()
            );

            log.info(
                    "Found {} unique CS2 skins, skipped {} without image, {} non-CS2",
                    cheapestByName.size(),
                    skippedWithoutImage.get(),
                    skippedNotCs2.get()
            );

            if (cheapestByName.isEmpty()) {
                throw new IllegalStateException(
                        "LIS snapshot returned no valid CS2 skins"
                );
            }

            List<LisSkinDocument> documents =
                    cheapestByName.values()
                            .stream()
                            .map(this::map)
                            .toList();

            log.info(
                    "Saving {} unique LIS skins to Redis...",
                    documents.size()
            );

            redisRepository.saveAll(documents);

            log.info(
                    "Loaded {} unique LIS skins into Redis",
                    documents.size()
            );

            log.info("Starting LIS WebSocket...");

            webSocketClient.connect();

        } catch (Exception e) {

            log.error(
                    "Failed to initialize LIS market",
                    e
            );
        }
    }

    private LisSkinSnapshotItem cheapest(
            LisSkinSnapshotItem current,
            LisSkinSnapshotItem candidate
    ) {

        return candidate.price().compareTo(current.price()) < 0
                ? candidate
                : current;
    }

    private LisSkinDocument map(LisSkinSnapshotItem item) {

        var metadata = metadataProvider.get(item.name());

        String rarity = metadata != null
                ? metadata.rarity()
                : null;

        return new LisSkinDocument(
                item.id(),
                item.name(),
                item.price(),
                parseFloat(item.itemFloat()),
                rarity,
                parseWear(item.name()),
                parseWeapon(item.name()),
                item.deliveryType(),
                item.itemClassId()
        );
    }

    private boolean hasImage(LisSkinSnapshotItem item) {

        return item.itemClassId() != null
                && !item.itemClassId().isBlank();
    }

    private Double parseFloat(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseWear(String name) {

        if (name == null) {
            return null;
        }

        int start = name.lastIndexOf('(');
        int end = name.lastIndexOf(')');

        if (start < 0 || end <= start) {
            return null;
        }

        String value = name.substring(start + 1, end);

        return switch (value) {
            case "Factory New",
                 "Minimal Wear",
                 "Field-Tested",
                 "Well-Worn",
                 "Battle-Scarred" -> value;
            default -> null;
        };
    }

    private String parseWeapon(String name) {

        if (name == null) {
            return null;
        }

        int separator = name.indexOf('|');

        if (separator < 0) {
            return null;
        }

        return name.substring(0, separator)
                .replace("StatTrak™", "")
                .replace("Souvenir", "")
                .replace("★", "")
                .trim();
    }
}
