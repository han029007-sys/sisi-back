package com.skinforge.upgrader.bll.service.withdraw.dto;

import com.skinforge.upgrader.model.WithdrawalStatus;

public record WithdrawalResponse(
        Long withdrawalId,
        Long inventoryId,
        String projectId,
        WithdrawalStatus status
) {
}