package com.skinforge.upgrader.bll.service.user;

import com.skinforge.upgrader.auth.dto.response.UserResponse;
import com.skinforge.upgrader.bll.service.balance.CurrencyPriceService;
import com.skinforge.upgrader.bll.service.upgrade.dto.BestWinResponse;
import com.skinforge.upgrader.bll.service.user.dto.InventorySkinResponse;
import com.skinforge.upgrader.bll.service.user.dto.SellSkinResponse;
import com.skinforge.upgrader.exception.InventoryNotFoundException;
import com.skinforge.upgrader.exception.UserNotFoundException;
import com.skinforge.upgrader.model.*;
import com.skinforge.upgrader.repository.BalanceTransactionRepository;
import com.skinforge.upgrader.repository.InventoryRepository;
import com.skinforge.upgrader.repository.UpgradeRepository;
import com.skinforge.upgrader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final UpgradeRepository upgradeRepository;

    public UserResponse getById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        long totalUpgrades =
                upgradeRepository.countByUserId(userId);

        long wins =
                upgradeRepository.countByUserIdAndResult(
                        userId,
                        UpgradeResult.WIN
                );

        long losses =
                upgradeRepository.countByUserIdAndResult(
                        userId,
                        UpgradeResult.LOSE
                );

        var bestWin = upgradeRepository
                .findTopByUserIdAndResultOrderByTargetPriceDesc(
                        userId,
                        UpgradeResult.WIN
                )
                .map(upgrade -> new BestWinResponse(
                        upgrade.getTargetMarketHashName(),
                        upgrade.getTargetImageUrl(),
                        upgrade.getTargetPrice()
                ))
                .orElse(null);

        return new UserResponse(
                user.getId(),
                user.getSteamId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getBalance(),
                user.getTradeUrl(),

                totalUpgrades,
                wins,
                losses,

                bestWin
        );
    }

    @Transactional
    public void updateTradeUrl(UUID userId, String tradeUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateTradeUrl(tradeUrl);
    }

    @Transactional(readOnly = true)
    public List<InventorySkinResponse> getInventory(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return inventoryRepository
                .findByUserIdAndStatus(userId, InventoryStatus.AVAILABLE)
                .stream()
                .map(item -> new InventorySkinResponse(
                        item.getId(),
                        item.getListingId(),
                        item.getMarketHashName(),
                        item.getImageUrl(),
                        item.getPrice(), // RUB
                        item.getRarity(),
                        item.getWear()
                ))
                .toList();
    }

    @Transactional
    public SellSkinResponse sell(UUID userId, long inventoryId) {
        InventoryItem item = inventoryRepository.findByIdAndUserIdForUpdate(inventoryId, userId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Пошел нахуй я не нашел это в инвентаре"
                        )
                );

        if (!item.getUserId().equals(userId)) {
            throw new IllegalStateException("Skin does not belong to user");
        }

        if (item.getStatus() != InventoryStatus.AVAILABLE) {
            throw new IllegalStateException("Skin is not available");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        BigDecimal sellPriceRub = item.getPrice();
        user.deposit(sellPriceRub);
        item.sell();

        balanceTransactionRepository.save(
                BalanceTransaction.skinSell(userId, item.getPrice(), item.getMarketHashName(),item.getImageUrl()));

        return new SellSkinResponse(
                item.getId(),
                sellPriceRub,
                user.getBalance()
        );
    }
}
