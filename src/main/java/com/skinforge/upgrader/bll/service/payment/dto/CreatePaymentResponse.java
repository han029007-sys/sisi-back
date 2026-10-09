package com.skinforge.upgrader.bll.service.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentId,
        String invoiceId,
        BigDecimal amount,
        String paymentUrl,
        String status
) {}