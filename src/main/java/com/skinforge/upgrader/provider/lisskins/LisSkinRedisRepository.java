package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.bll.service.skin.dto.PriceSort;
import com.skinforge.upgrader.provider.lisskins.dto.LisSkinDocument;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.output.NestedMultiOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LisSkinRedisRepository {

    private static final String PREFIX = "lis:skin:";
    private static final String LISTING_PREFIX = "lis:listing:";
    private static final String INDEX = "idx:lis:skins";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> searchConnection;

    @PostConstruct
    void initSearchConnection() {

        redisClient = RedisClient.create(
                "redis://" + redisHost + ":" + redisPort
        );

        searchConnection = redisClient.connect();
    }

    @PreDestroy
    void closeSearchConnection() {

        if (searchConnection != null) {
            searchConnection.close();
        }

        if (redisClient != null) {
            redisClient.shutdown();
        }
    }

    public void saveAll(List<LisSkinDocument> skins) {

        int batchSize = 1000;

        for (int from = 0; from < skins.size(); from += batchSize) {

            int to = Math.min(
                    from + batchSize,
                    skins.size()
            );

            List<LisSkinDocument> batch =
                    skins.subList(from, to);

            redisTemplate.executePipelined(
                    (RedisCallback<Object>) connection -> {

                        for (LisSkinDocument skin : batch) {

                            try {
                                String json =
                                        objectMapper.writeValueAsString(skin);

                                connection.execute(
                                        "JSON.SET",
                                        bytes(key(skin.name())),
                                        bytes("$"),
                                        bytes(json)
                                );

                                connection.stringCommands().set(
                                        bytes(LISTING_PREFIX + skin.id()),
                                        bytes(normalizeName(skin.name()))
                                );

                            } catch (JacksonException e) {

                                throw new IllegalStateException(
                                        "Failed to serialize LIS skin "
                                                + skin.id(),
                                        e
                                );
                            }
                        }

                        return null;
                    }
            );
        }
    }

    public synchronized void save(
            LisSkinDocument skin
    ) {

        String normalizedName =
                normalizeName(skin.name());

        LisSkinDocument old =
                findByName(normalizedName);

        try {
            String json =
                    objectMapper.writeValueAsString(skin);

            redisTemplate.execute(
                    (RedisCallback<Object>) connection -> {

                        if (old != null
                                && !old.id().equals(skin.id())) {

                            connection.keyCommands().del(
                                    bytes(
                                            LISTING_PREFIX
                                                    + old.id()
                                    )
                            );
                        }

                        connection.execute(
                                "JSON.SET",
                                bytes(key(normalizedName)),
                                bytes("$"),
                                bytes(json)
                        );

                        connection.stringCommands().set(
                                bytes(
                                        LISTING_PREFIX
                                                + skin.id()
                                ),
                                bytes(normalizedName)
                        );

                        return null;
                    }
            );

        } catch (JacksonException e) {

            throw new IllegalStateException(
                    "Failed to serialize LIS skin "
                            + skin.id(),
                    e
            );
        }
    }

    public LisSkinDocument findByName(
            String name
    ) {

        String normalizedName =
                normalizeName(name);

        Object result =
                redisTemplate.execute(
                        (RedisCallback<Object>) connection ->
                                connection.execute(
                                        "JSON.GET",
                                        bytes(
                                                key(normalizedName)
                                        )
                                )
                );

        if (result == null) {
            return null;
        }

        try {
            return objectMapper.readValue(
                    asString(result),
                    LisSkinDocument.class
            );

        } catch (JacksonException e) {

            throw new IllegalStateException(
                    "Failed to deserialize LIS skin "
                            + normalizedName,
                    e
            );
        }
    }

    public LisSkinDocument findByListingId(
            Long listingId
    ) {

        String name =
                redisTemplate
                        .opsForValue()
                        .get(
                                LISTING_PREFIX
                                        + listingId
                        );

        if (name == null) {
            return null;
        }

        LisSkinDocument skin =
                findByName(name);

        if (skin == null
                || !skin.id().equals(listingId)) {
            return null;
        }

        return skin;
    }

    public void deleteByName(
            String name
    ) {

        String normalizedName =
                normalizeName(name);

        LisSkinDocument current =
                findByName(normalizedName);

        if (current == null) {
            return;
        }

        redisTemplate.delete(
                key(normalizedName)
        );

        redisTemplate.delete(
                LISTING_PREFIX
                        + current.id()
        );
    }

    public void clearAll() {

        Set<String> skinKeys =
                redisTemplate.keys(
                        PREFIX + "*"
                );

        if (skinKeys != null
                && !skinKeys.isEmpty()) {

            redisTemplate.delete(
                    skinKeys
            );
        }

        Set<String> listingKeys =
                redisTemplate.keys(
                        LISTING_PREFIX + "*"
                );

        if (listingKeys != null
                && !listingKeys.isEmpty()) {

            redisTemplate.delete(
                    listingKeys
            );
        }
    }

    public boolean isEmpty() {

        Set<String> keys =
                redisTemplate.keys(
                        PREFIX + "*"
                );

        return keys == null
                || keys.isEmpty();
    }

    public List<LisSkinDocument> search(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            PriceSort priceSort,
            int offset,
            int limit
    ) {

        String min =
                minPrice != null
                        ? minPrice.toPlainString()
                        : "-inf";

        String max =
                maxPrice != null
                        ? maxPrice.toPlainString()
                        : "+inf";

        String query =
                "@price:["
                        + min
                        + " "
                        + max
                        + "]";

        var codec =
                searchConnection.getCodec();

        var args =
                new CommandArgs<String, String>(codec)
                        .add(INDEX)
                        .add(query)

                        .add("RETURN")
                        .add(1)
                        .add("$")

                        .add("SORTBY")
                        .add("price")
                        .add(
                                priceSort == PriceSort.ASC
                                        ? "ASC"
                                        : "DESC"
                        )

                        .add("LIMIT")
                        .add(offset)
                        .add(limit)

                        .add("TIMEOUT")
                        .add(5000);

        List<Object> response =
                searchConnection
                        .sync()
                        .dispatch(
                                RedisSearchCommand.FT_SEARCH,
                                new NestedMultiOutput<>(codec),
                                args
                        );

        List<LisSkinDocument> result =
                new ArrayList<>();

        extractSkins(
                response,
                result
        );

        return result;
    }

    private void extractSkins(
            Object value,
            List<LisSkinDocument> result
    ) {

        if (value == null) {
            return;
        }

        if (value instanceof List<?> list) {

            for (Object item : list) {
                extractSkins(
                        item,
                        result
                );
            }

            return;
        }

        String text =
                asString(value);

        if (text == null) {
            return;
        }

        text =
                text.trim();

        if (!text.startsWith("{")) {
            return;
        }

        try {
            LisSkinDocument skin =
                    objectMapper.readValue(
                            text,
                            LisSkinDocument.class
                    );

            result.add(skin);

        } catch (JacksonException ignored) {
        }
    }

    private String key(
            String name
    ) {

        String normalizedName =
                normalizeName(name);

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            normalizedName.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return PREFIX
                    + HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 is not available",
                    e
            );
        }
    }

    private String normalizeName(
            String name
    ) {

        if (name == null) {
            throw new IllegalArgumentException(
                    "Skin name cannot be null"
            );
        }

        return name
                .trim()
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ");
    }

    private byte[] bytes(
            String value
    ) {

        return value.getBytes(
                StandardCharsets.UTF_8
        );
    }

    private String asString(
            Object value
    ) {

        if (value instanceof byte[] bytes) {

            return new String(
                    bytes,
                    StandardCharsets.UTF_8
            );
        }

        return String.valueOf(
                value
        );
    }

    private enum RedisSearchCommand
            implements ProtocolKeyword {

        FT_SEARCH("FT.SEARCH");

        private final byte[] bytes;

        RedisSearchCommand(
                String command
        ) {

            this.bytes =
                    command.getBytes(
                            StandardCharsets.UTF_8
                    );
        }

        @Override
        public byte[] getBytes() {
            return bytes;
        }
    }
}