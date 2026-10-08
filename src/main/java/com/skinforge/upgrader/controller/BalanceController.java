package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.balance.BalanceService;
import com.skinforge.upgrader.bll.service.balance.dto.BalanceTransactionResponse;
import com.skinforge.upgrader.controller.request.DepositBalanceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/deposit/test")
    public void deposit(Authentication authentication, @RequestBody DepositBalanceRequest request) {
        return;
//        UUID userId = (UUID) authentication.getPrincipal();
//
//        balanceService.depositTest(userId, request.amount());
    }

    @GetMapping("/transactions")
    public Page<BalanceTransactionResponse> getTransactions(
            Authentication authentication,
            Pageable pageable
    ) {
        UUID userId = (UUID) authentication.getPrincipal();

        return balanceService.getTransactions(
                userId,
                pageable
        );
    }
}
