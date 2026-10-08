package com.skinforge.upgrader.provider.lisskins.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class LisWebSocketClient {

    private static final URI WS_URI = URI.create(
            "wss://ws.lis-skins.com/connection/websocket"
                    + "?cf_ws_frame_ping_pong=true"
    );

    private final LisWsTokenClient tokenClient;
    private final LisMarketEventHandler eventHandler;
    private final ObjectMapper objectMapper;

    private final WebSocketClient webSocketClient =
            new StandardWebSocketClient();

    public void connect() {

        String token = tokenClient.getToken();

        LisWebSocketHandler handler =
                new LisWebSocketHandler(
                        token,
                        objectMapper,
                        eventHandler,
                        this::reconnect
                );

        webSocketClient.execute(
                handler,
                String.valueOf(WS_URI)
        );
    }

    private void reconnect() {

        log.warn("Reconnecting LIS websocket...");

        Thread.ofVirtual().start(() -> {

            try {
                Thread.sleep(3000);

                connect();

            } catch (Exception e) {

                log.error(
                        "Failed to reconnect LIS websocket",
                        e
                );
            }
        });
    }
}