package com.skinforge.upgrader.bll.service.upgrade;

import com.skinforge.upgrader.bll.service.skin.SkinMarketService;
import com.skinforge.upgrader.bll.service.upgrade.dto.UpgradeHistoryResponse;
import com.skinforge.upgrader.bll.service.upgrade.dto.UpgradeResponse;
import com.skinforge.upgrader.controller.request.UpgradeRequest;
import com.skinforge.upgrader.exception.InventoryNotFoundException;
import com.skinforge.upgrader.model.*;
import com.skinforge.upgrader.repository.InventoryRepository;
import com.skinforge.upgrader.repository.UpgradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UpgradeService {

    private final InventoryRepository inventoryRepository;
    private final UpgradeRepository upgradeRepository;
    private final SkinMarketService skinMarketService;

    private final BigDecimal RTP = new BigDecimal("0.80");
    private static final BigDecimal MAX_CHANCE = new BigDecimal("0.80");
    private static final BigDecimal MAX_WIN_AMOUNT = new BigDecimal("100.00");

    public Page<UpgradeHistoryResponse> getHistory(UUID userId, Pageable pageable) {
        return upgradeRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(upgrade -> new UpgradeHistoryResponse(
                        upgrade.getId(),

                        upgrade.getInputMarketHashName(),
                        upgrade.getInputImageUrl(),
                        upgrade.getInputPrice(),

                        upgrade.getTargetMarketHashName(),
                        upgrade.getTargetImageUrl(),
                        upgrade.getTargetPrice(),

                        upgrade.getChance(),
                        upgrade.getRoll(),
                        upgrade.getResult(),

                        upgrade.getCreatedAt()
                ));
    }

    @Transactional
    public UpgradeResponse upgrade(UUID userId, UpgradeRequest request) {
        var input = inventoryRepository.findByIdAndUserIdForUpdate(request.inputInventoryId(), userId)
                .orElseThrow(() -> new InventoryNotFoundException("Не найдена хуйня в инвентаре"));

        if (!input.getUserId().equals(userId)) {
            throw new IllegalStateException("Skin does not belong to user");
        }

        if (input.getStatus() != InventoryStatus.AVAILABLE) {
            throw new IllegalStateException("Skin is not available");
        }

        var target = skinMarketService.findByListingId(request.targetListingId());

        BigDecimal inputPrice = input.getPrice();
        BigDecimal targetPrice = target.price();

        if (targetPrice.compareTo(inputPrice) <= 0) {
            throw new IllegalArgumentException("Target skin must be more expensive");
        }

        BigDecimal chance = inputPrice
                .divide(targetPrice, 8, RoundingMode.DOWN)
                .multiply(RTP)
                .min(getMaxChance(userId))
                .setScale(8, RoundingMode.DOWN);

        BigDecimal chanceReal = inputPrice
                .divide(targetPrice, 8, RoundingMode.DOWN)
                .setScale(8, RoundingMode.DOWN);

        BigDecimal roll = BigDecimal.valueOf(
                ThreadLocalRandom.current().nextDouble()
        ).setScale(8, RoundingMode.DOWN);

        boolean win = roll.compareTo(chance) < 0;

        input.consume();

        Long outputInventoryId = null;

        if (win) {
            InventoryItem output = inventoryRepository.save(
                    new InventoryItem(
                            userId,
                            target.listingId(),
                            target.marketHashName(),
                            target.imageUrl(),
                            target.price(),
                            target.rarity(),
                            target.wear()
                    )
            );

            outputInventoryId = output.getId();
        }

        UpgradeResult result = win
                ? UpgradeResult.WIN
                : UpgradeResult.LOSE;

        Upgrade upgrade = new Upgrade(
                userId,
                input.getId(),
                outputInventoryId,

                input.getListingId(),
                input.getMarketHashName(),
                input.getImageUrl(),
                input.getPrice(),

                target.listingId(),
                target.marketHashName(),
                target.imageUrl(),
                target.price(),

                chanceReal,
                roll,
                result
        );

        upgradeRepository.save(upgrade);

        return new UpgradeResponse(
                upgrade.getId(),
                win ? target : null
        );
    }

    private BigDecimal getMaxChance(UUID userId){
        BigDecimal pnl = upgradeRepository.calculateUserPnl(userId);;

        if (pnl.compareTo(MAX_WIN_AMOUNT) <= 0){
            return new BigDecimal("0");
        }
        else {
            return MAX_CHANCE;
        }
    }
}
