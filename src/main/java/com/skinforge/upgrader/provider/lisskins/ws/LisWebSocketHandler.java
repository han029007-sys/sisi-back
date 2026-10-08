package com.skinforge.upgrader.provider.lisskins.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Slf4j
public class LisWebSocketHandler extends TextWebSocketHandler {

    private static final int CONNECT_REQUEST_ID = 1;
    private static final int SUBSCRIBE_REQUEST_ID = 2;

    private static final String MARKET_CHANNEL =
            "public:obtained-skins";

    private final String token;
    private final ObjectMapper objectMapper;
    private final LisMarketEventHandler eventHandler;
    private final Runnable reconnectCallback;

    public LisWebSocketHandler(
            String token,
            ObjectMapper objectMapper,
            LisMarketEventHandler eventHandler,
            Runnable reconnectCallback
    ) {
        this.token = token;
        this.objectMapper = objectMapper;
        this.eventHandler = eventHandler;
        this.reconnectCallback = reconnectCallback;
    }

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session
    ) throws Exception {

        log.info("LIS websocket connected");

        ObjectNode root =
                objectMapper.createObjectNode();

        root.put(
                "id",
                CONNECT_REQUEST_ID
        );

        ObjectNode connect =
                root.putObject("connect");

        connect.put(
                "token",
                token
        );

        session.sendMessage(
                new TextMessage(
                        objectMapper.writeValueAsString(root)
                )
        );

        log.info("LIS connect command sent");
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    ) throws Exception {

        String payload = message.getPayload();

        try (var values = objectMapper
                .readerFor(JsonNode.class)
                .readValues(payload)) {

            while (values.hasNextValue()) {

                JsonNode root = (JsonNode) values.nextValue();

                if (root != null) {
                    handleMessage(session, root);
                }
            }
        }
    }

    private void handleMessage(
            WebSocketSession session,
            JsonNode root
    ) throws Exception {

        // application-level ping
        if (root.isObject()
                && root.isEmpty()) {

            session.sendMessage(
                    new TextMessage("{}")
            );

            return;
        }

        if (root.has("error")) {

            log.error(
                    "LIS websocket error: {}",
                    root.toPrettyString()
            );

            return;
        }

        if (isResponse(
                root,
                CONNECT_REQUEST_ID
        )) {

            handleConnectResponse(
                    session,
                    root
            );

            return;
        }

        if (isResponse(
                root,
                SUBSCRIBE_REQUEST_ID
        )) {

            handleSubscribeResponse(root);

            return;
        }

        if (root.has("push")) {

            eventHandler.handle(
                    root.get("push")
            );
        }
    }

    private void handleConnectResponse(
            WebSocketSession session,
            JsonNode root
    ) throws Exception {

        if (!root.has("connect")) {

            log.error(
                    "Unexpected LIS connect response: {}",
                    root.toPrettyString()
            );

            return;
        }

        JsonNode connect =
                root.get("connect");

        log.info(
                "LIS connected: client={}, ttl={}",
                connect.path("client").asText(),
                connect.path("ttl").asInt()
        );

        subscribe(session);
    }

    private void subscribe(
            WebSocketSession session
    ) throws Exception {

        ObjectNode root =
                objectMapper.createObjectNode();

        root.put(
                "id",
                SUBSCRIBE_REQUEST_ID
        );

        ObjectNode subscribe =
                root.putObject("subscribe");

        subscribe.put(
                "channel",
                MARKET_CHANNEL
        );

        session.sendMessage(
                new TextMessage(
                        objectMapper.writeValueAsString(root)
                )
        );

        log.info(
                "LIS subscribe sent: {}",
                MARKET_CHANNEL
        );
    }

    private void handleSubscribeResponse(
            JsonNode root
    ) {

        if (root.has("subscribe")) {

            log.info(
                    "LIS subscribed successfully: {}",
                    MARKET_CHANNEL
            );

            return;
        }

        log.warn(
                "Unexpected LIS subscription response: {}",
                root.toPrettyString()
        );
    }

    private boolean isResponse(
            JsonNode root,
            int id
    ) {

        return root.has("id")
                && root.get("id").asInt() == id;
    }

    @Override
    public void handleTransportError(
            WebSocketSession session,
            Throwable exception
    ) {

        log.error(
                "LIS websocket transport error",
                exception
        );
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {

        log.warn(
                "LIS websocket closed: code={}, reason={}",
                status.getCode(),
                status.getReason()
        );

        reconnectCallback.run();
    }
}