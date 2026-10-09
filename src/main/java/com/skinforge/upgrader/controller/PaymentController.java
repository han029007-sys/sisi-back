package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.payment.PaymentService;
import com.skinforge.upgrader.bll.service.payment.dto.CreatePaymentRequest;
import com.skinforge.upgrader.bll.service.payment.dto.CreatePaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> create(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        return ResponseEntity.ok(
                paymentService.createPayment(userId, request)
        );
    }
}