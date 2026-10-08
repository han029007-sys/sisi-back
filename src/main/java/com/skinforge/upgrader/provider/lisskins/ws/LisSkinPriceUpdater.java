package com.skinforge.upgrader.provider.lisskins.ws;

import com.skinforge.upgrader.metadata.SkinMetadataProvider;
import com.skinforge.upgrader.provider.lisskins.LisSkinRedisRepository;
import com.skinforge.upgrader.provider.lisskins.dto.LisSkinDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LisSkinPriceUpdater {

    private final LisSkinRedisRepository redisRepository;
    private final SkinMetadataProvider metadataProvider;

    private final ConcurrentHashMap<String, Object> locks =
            new ConcurrentHashMap<>();

    public void onAdded(JsonNode data) {

        String name = readName(data);

        if (name == null) {
            return;
        }

        synchronized (lock(name)) {
            handleAdded(data);
        }
    }

    public void onPriceChanged(JsonNode data) {

        String name = readName(data);

        if (name == null) {
            return;
        }

        synchronized (lock(name)) {
            handlePriceChanged(data);
        }
    }

    public void onDeleted(
            Long listingId,
            String name
    ) {

        if (name == null || name.isBlank()) {
            return;
        }

        synchronized (lock(name)) {
            handleDeleted(
                    listingId,
                    name
            );
        }
    }

    private void handleAdded(
            JsonNode data
    ) {

        LisSkinDocument incoming =
                toDocument(data);

        if (incoming == null) {
            return;
        }

        LisSkinDocument current =
                redisRepository.findByName(
                        incoming.name()
                );

        /*
         * Этого скина ещё нет.
         */
        if (current == null) {

            redisRepository.save(
                    incoming
            );

            log.debug(
                    "Added cheapest: {} {}",
                    incoming.name(),
                    incoming.price()
            );

            return;
        }

        /*
         * Новый listing дешевле текущего.
         *
         * save() пишет в тот же key по name,
         * поэтому старый документ автоматически
         * заменяется.
         */
        if (incoming.price()
                .compareTo(current.price()) < 0) {

            redisRepository.save(
                    incoming
            );

            log.debug(
                    "Cheapest replaced: {} {} -> {}",
                    incoming.name(),
                    current.price(),
                    incoming.price()
            );
        }
    }

    private void handlePriceChanged(
            JsonNode data
    ) {

        LisSkinDocument incoming =
                toDocument(data);

        if (incoming == null) {
            return;
        }

        LisSkinDocument current =
                redisRepository.findByName(
                        incoming.name()
                );

        /*
         * Почему-то такого name нет в Redis.
         */
        if (current == null) {

            redisRepository.save(
                    incoming
            );

            return;
        }

        /*
         * Изменилась цена именно того listing,
         * который сейчас лежит в Redis.
         */
        if (current.id().equals(
                incoming.id()
        )) {

            /*
             * Цена стала меньше или равна.
             * Он всё ещё cheapest.
             *
             * При этом сохраняем старую metadata,
             * меняя только то, что реально пришло.
             */
            if (incoming.price()
                    .compareTo(current.price()) <= 0) {

                LisSkinDocument updated =
                        new LisSkinDocument(
                                current.id(),
                                current.name(),
                                incoming.price(),

                                incoming.floatValue() != null
                                        ? incoming.floatValue()
                                        : current.floatValue(),

                                current.rarity(),
                                current.wear(),
                                current.weapon(),
                                current.deliveryType(),

                                incoming.itemClassId() != null
                                        ? incoming.itemClassId()
                                        : current.itemClassId()
                        );

                redisRepository.save(
                        updated
                );

                log.debug(
                        "Cheapest price updated: {} {} -> {}",
                        current.name(),
                        current.price(),
                        incoming.price()
                );

                return;
            }

            /*
             * Цена текущего cheapest выросла.
             *
             * Здесь нельзя просто сохранить новую цену,
             * потому что другой LIS listing этого же name
             * мог стать дешевле.
             *
             * Здесь потом будет:
             *
             * refreshCheapest(current.name());
             */

            log.debug(
                    "Current cheapest increased: {} {} -> {}",
                    current.name(),
                    current.price(),
                    incoming.price()
            );

            return;
        }

        /*
         * Изменилась цена другого listing.
         *
         * Если он стал дешевле текущего —
         * просто перезаписываем один и тот же
         * Redis key.
         */
        if (incoming.price()
                .compareTo(current.price()) < 0) {

            redisRepository.save(
                    incoming
            );

            log.debug(
                    "New cheapest after price change: {} {} -> {}",
                    current.name(),
                    current.price(),
                    incoming.price()
            );
        }
    }

    private void handleDeleted(
            Long listingId,
            String name
    ) {

        LisSkinDocument current =
                redisRepository.findByName(name);

        if (current == null) {
            return;
        }

        /*
         * Удалили не тот listing,
         * который сейчас хранится как cheapest.
         */
        if (!current.id().equals(
                listingId
        )) {
            return;
        }

        /*
         * Удалили текущий cheapest.
         */
        redisRepository.deleteByName(
                name
        );

        log.debug(
                "Current cheapest deleted: {} id={}",
                name,
                listingId
        );

        /*
         * ВАЖНО:
         *
         * После удаления надо будет найти
         * следующий cheapest через LIS REST:
         *
         * refreshCheapest(name);
         */
    }

    private LisSkinDocument toDocument(
            JsonNode data
    ) {

        long id =
                data.path("id").asLong();

        String name =
                readName(data);

        if (id == 0 || name == null) {
            return null;
        }

        var metadata =
                metadataProvider.get(name);

        if (metadata == null) {
            return null;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(
                    data.path("price").asText()
            );
        } catch (Exception e) {
            return null;
        }

        Double floatValue =
                readDouble(
                        data.get("item_float")
                );

        String classId =
                readString(
                        data.get("item_class_id")
                );

        return new LisSkinDocument(
                id,
                name,
                price,
                floatValue,
                metadata.rarity(),
                parseWear(name),
                parseWeapon(name),
                readInteger(
                        data.get("delivery_type")
                ),
                classId
        );
    }

    private String readName(
            JsonNode data
    ) {

        if (data == null) {
            return null;
        }

        String name =
                data.path("name").asText();

        if (name == null
                || name.isBlank()) {
            return null;
        }

        return normalizeName(name);
    }

    private String normalizeName(
            String name
    ) {

        return name
                .trim()
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ");
    }

    private Object lock(
            String name
    ) {

        return locks.computeIfAbsent(
                normalizeName(name),
                ignored -> new Object()
        );
    }

    private String parseWear(
            String name
    ) {

        if (name == null) {
            return null;
        }

        int start =
                name.lastIndexOf('(');

        int end =
                name.lastIndexOf(')');

        if (start < 0 || end <= start) {
            return null;
        }

        String value =
                name.substring(
                        start + 1,
                        end
                );

        return switch (value) {

            case "Factory New",
                 "Minimal Wear",
                 "Field-Tested",
                 "Well-Worn",
                 "Battle-Scarred" ->
                    value;

            default ->
                    null;
        };
    }

    private String parseWeapon(
            String name
    ) {

        if (name == null) {
            return null;
        }

        int separator =
                name.indexOf('|');

        if (separator < 0) {
            return null;
        }

        return name.substring(
                        0,
                        separator
                )
                .replace("StatTrak™", "")
                .replace("Souvenir", "")
                .replace("★", "")
                .trim();
    }

    private String readString(
            JsonNode node
    ) {

        if (node == null
                || node.isNull()) {
            return null;
        }

        String value =
                node.asText();

        return value.isBlank()
                ? null
                : value;
    }

    private Double readDouble(
            JsonNode node
    ) {

        String value =
                readString(node);

        if (value == null) {
            return null;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer readInteger(
            JsonNode node
    ) {

        if (node == null
                || node.isNull()) {
            return null;
        }

        try {
            return node.asInt();
        } catch (Exception e) {
            return null;
        }
    }
}