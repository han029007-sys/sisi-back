package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.user.UserService;
import com.skinforge.upgrader.controller.request.TradeUrlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/me/trade-url")
    public void updateTradeUrl(Authentication authentication, @RequestBody TradeUrlRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();

        userService.updateTradeUrl(userId, request.tradeUrl());
    }
}
