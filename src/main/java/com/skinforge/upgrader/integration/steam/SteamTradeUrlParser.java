package com.skinforge.upgrader.integration.steam;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SteamTradeUrlParser {

    public TradeData parse(String tradeUrl) {

        if (tradeUrl == null
                || tradeUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "Steam Trade URL is empty"
            );
        }

        URI uri =
                URI.create(tradeUrl);

        if (!"steamcommunity.com"
                .equalsIgnoreCase(uri.getHost())) {

            throw new IllegalArgumentException(
                    "Invalid Steam Trade URL"
            );
        }

        String query =
                uri.getRawQuery();

        if (query == null) {
            throw new IllegalArgumentException(
                    "Invalid Steam Trade URL"
            );
        }

        Map<String, String> params =
                Arrays.stream(query.split("&"))
                        .map(value ->
                                value.split("=", 2)
                        )
                        .filter(value ->
                                value.length == 2
                        )
                        .collect(
                                Collectors.toMap(
                                        value ->
                                                decode(value[0]),
                                        value ->
                                                decode(value[1]),
                                        (first, second) ->
                                                first
                                )
                        );

        String partner =
                params.get("partner");

        String token =
                params.get("token");

        if (partner == null
                || token == null) {

            throw new IllegalArgumentException(
                    "Trade URL must contain partner and token"
            );
        }

        return new TradeData(
                partner,
                token
        );
    }

    private String decode(
            String value
    ) {

        return URLDecoder.decode(
                value,
                StandardCharsets.UTF_8
        );
    }

    public record TradeData(
            String partner,
            String token
    ) {
    }
}