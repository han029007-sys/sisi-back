package com.skinforge.upgrader.bll.service.balance.dto;

import com.skinforge.upgrader.model.BalanceTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BalanceTransactionResponse(
        Long id,
        BalanceTransactionType type,
        BigDecimal amount,
        String skinName,
        String skinImageUrl,
        LocalDateTime createdAt
) {
}
