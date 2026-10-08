package com.skinforge.upgrader.controller;

import com.skinforge.upgrader.bll.service.user.UserService;
import com.skinforge.upgrader.bll.service.user.dto.InventorySkinResponse;
import com.skinforge.upgrader.bll.service.user.dto.SellSkinResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final UserService userService;

    public InventoryController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<InventorySkinResponse> deposit(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return userService.getInventory(userId);
    }

    @PostMapping("/{id}/sell")
    public SellSkinResponse sell(@PathVariable Long id, Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        return userService.sell(userId, id);
    }
}
