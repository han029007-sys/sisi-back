
package com.skinforge.upgrader.provider.lisskins;

import com.skinforge.upgrader.provider.lisskins.dto.LisSkinSnapshotItem;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

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

    public void forEachSkin(
            Consumer<LisSkinSnapshotItem> consumer
    ) {
        restClient.get()
                .uri(URL)
                .exchange((request, response) -> {

                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException(
                                "LIS snapshot HTTP error: "
                                        + response.getStatusCode()
                        );
                    }

                    try (JsonParser parser =
                                 objectMapper.createParser(response.getBody())) {

                        if (parser.nextToken() != JsonToken.START_OBJECT) {
                            throw new IllegalStateException(
                                    "Unexpected LIS snapshot format"
                            );
                        }

                        boolean foundItems = false;
                        JsonToken token;

                        while ((token = parser.nextToken()) != null) {

                            if (token == JsonToken.END_OBJECT) {
                                break;
                            }

                            if (token != JsonToken.PROPERTY_NAME) {
                                throw new IllegalStateException(
                                        "Unexpected snapshot token: " + token
                                );
                            }

                            String fieldName = parser.currentName();
                            JsonToken valueToken = parser.nextToken();

                            if ("items".equals(fieldName)
                                    && valueToken == JsonToken.START_ARRAY) {

                                foundItems = true;

                                JsonToken firstToken = parser.nextToken();

                                if (firstToken == null) {
                                    throw new IllegalStateException(
                                            "Unexpected end of LIS items array"
                                    );
                                }

                                if (firstToken == JsonToken.END_ARRAY) {
                                    continue;
                                }

                                if (firstToken != JsonToken.START_OBJECT) {
                                    throw new IllegalStateException(
                                            "Expected skin object, got: " + firstToken
                                    );
                                }

                                try (var iterator = objectMapper
                                        .readerFor(LisSkinSnapshotItem.class)
                                        .<LisSkinSnapshotItem>readValues(parser)) {

                                    while (iterator.hasNextValue()) {
                                        LisSkinSnapshotItem skin = iterator.nextValue();
                                        consumer.accept(skin);
                                    }
                                }
                            } else {
                                parser.skipChildren();
                            }
                        }

                        if (!foundItems) {
                            throw new IllegalStateException(
                                    "LIS snapshot does not contain items array"
                            );
                        }

                    } catch (Exception e) {
                        throw new IllegalStateException(
                                "Failed to process LIS snapshot",
                                e
                        );
                    }

                    return null;
                });
    }
}
