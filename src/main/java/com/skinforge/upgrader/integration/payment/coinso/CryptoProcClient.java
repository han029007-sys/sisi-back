package com.skinforge.upgrader.integration.payment.coinso;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CryptoProcClient {

    private final RestClient restClient;
    private final long projectId;

    public CryptoProcClient(
            @Value("${cryptoproc.base-url}") String baseUrl,
            @Value("${cryptoproc.project-id}") long projectId,
            @Value("${cryptoproc.secret-key}") String secretKey
    ) {
        this.projectId = projectId;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + secretKey)
                .build();
    }

    public CreateInvoiceResponse createInvoice(
            BigDecimal amountRub,
            String paymentId,
            String clientEmail
    ) {
        var body = Map.of(
                "project_id", projectId,
                "amount", amountRub,
                "description", "SisiSkins balance top-up",
                "custom", paymentId,
                "method", "crypto",
                "integration_type", "standard",
                "client_email", clientEmail,
                "success_url", "https://sisiskins.best/payment/result",
                "fail_url", "https://sisiskins.best/payment/result"
        );

        return restClient.post()
                .uri("/payment/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(CreateInvoiceResponse.class);
    }

    public PaymentStatusResponse getStatus(String invoiceId) {
        return restClient.get()
                .uri(uri -> uri
                        .path("/payment/status")
                        .queryParam("uuid", invoiceId)
                        .build())
                .retrieve()
                .body(PaymentStatusResponse.class);
    }

    public record CreateInvoiceResponse(
            boolean success,
            String invoice_id,
            String payment_url,
            String message
    ) {}

    public record PaymentStatusResponse(
            boolean success,
            String status,
            String invoice_id,
            String custom,
            BigDecimal amount
    ) {}
}
