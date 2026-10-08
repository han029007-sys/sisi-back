package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.withdraw.WithdrawalService;
import com.skinforge.upgrader.bll.service.withdraw.dto.WithdrawalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/withdraw")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/{inventoryId}/withdraw")
    public WithdrawalResponse withdraw(@PathVariable Long inventoryId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return withdrawalService.withdraw(userId, inventoryId);
    }

    @GetMapping("/{projectId}/status")
    public WithdrawalResponse status(@PathVariable String projectId, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return withdrawalService.checkStatus(userId, projectId);
    }
}