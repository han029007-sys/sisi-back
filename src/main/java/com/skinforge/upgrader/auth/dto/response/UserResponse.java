package com.skinforge.upgrader.auth.dto.response;

import com.skinforge.upgrader.bll.service.upgrade.dto.BestWinResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String steamId,
        String username,
        String avatarUrl,
        BigDecimal balance,
        String tradeUrl,

        long totalUpgrades,
        long wins,
        long losses,

        BestWinResponse bestWin
) {
}