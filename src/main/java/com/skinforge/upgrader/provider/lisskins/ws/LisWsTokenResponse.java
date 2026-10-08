package com.skinforge.upgrader.provider.lisskins.ws;

public record LisWsTokenResponse(
        Data data
) {
    public record Data(
            String token
    ) {
    }
}