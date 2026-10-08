package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.balance.BalanceService;
import com.skinforge.upgrader.bll.service.upgrade.UpgradeService;
import com.skinforge.upgrader.bll.service.upgrade.dto.UpgradeHistoryResponse;
import com.skinforge.upgrader.bll.service.upgrade.dto.UpgradeResponse;
import com.skinforge.upgrader.controller.request.DepositBalanceRequest;
import com.skinforge.upgrader.controller.request.UpgradeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/upgrade")
public class UpgradeController {
    private final UpgradeService upgradeService;

    public UpgradeController(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @PostMapping()
    public UpgradeResponse upgrade(Authentication authentication, @RequestBody UpgradeRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();

        return upgradeService.upgrade(userId, request);
    }


    @GetMapping("/history")
    public Page<UpgradeHistoryResponse> history(Authentication authentication,
                                                @PageableDefault(size = 20) Pageable pageable
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        return upgradeService.getHistory(userId, pageable);
    }
}
