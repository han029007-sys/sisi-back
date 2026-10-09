package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.payment.PaymentConfirmationService;
import com.skinforge.upgrader.controller.request.CryptoProcWebhook;
import com.skinforge.upgrader.validator.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final WebhookSignatureValidator signatureValidator;
    private final PaymentConfirmationService confirmationService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody byte[] rawBody,
            @RequestHeader(
                    value = "X-Signature",
                    required = false
            ) String signature
    ) {

        if (!signatureValidator.isValid(rawBody, signature)) {
            log.warn("Invalid CryptoProc webhook signature");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        CryptoProcWebhook webhook;

        try {
            webhook = objectMapper.readValue(
                    rawBody,
                    CryptoProcWebhook.class
            );
        } catch (Exception e) {
            log.warn("Invalid CryptoProc webhook payload", e);

            return ResponseEntity.badRequest().build();
        }

        if (webhook.invoice_id() == null ||
                webhook.invoice_id().isBlank()) {

            return ResponseEntity.badRequest().build();
        }

        if (!"payment.success".equals(webhook.event())
                && !"payment.failed".equals(webhook.event())) {

            log.warn(
                    "Ignoring unsupported CryptoProc event: {}",
                    webhook.event()
            );

            return ResponseEntity.ok().build();
        }
        confirmationService.confirm(webhook.invoice_id());
        return ResponseEntity.ok().build();
    }
}