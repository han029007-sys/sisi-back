package com.skinforge.upgrader.controller.request;

public record CryptoProcWebhook(
        String event,
        String invoice_id,
        String custom,
        String status
) {}
