package com.skinforge.upgrader.bll.service.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull
        @DecimalMin("1.00")
        BigDecimal amount,

        @NotNull
        @Email
        String email
) {}