package com.skinforge.upgrader.bll.service.skin;

import com.skinforge.upgrader.bll.service.skin.dto.PriceSort;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.exception.InsufficientBalanceException;
import com.skinforge.upgrader.exception.UserNotFoundException;
import com.skinforge.upgrader.model.BalanceTransaction;
import com.skinforge.upgrader.model.InventoryItem;
import com.skinforge.upgrader.repository.BalanceTransactionRepository;
import com.skinforge.upgrader.repository.InventoryRepository;
import com.skinforge.upgrader.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkinPurchaseService {

    private final SkinMarketService marketService;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;

    public SkinPurchaseService(SkinMarketService marketService, UserRepository userRepository, InventoryRepository inventoryRepository, BalanceTransactionRepository balanceTransactionRepository) {
        this.marketService = marketService;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.balanceTransactionRepository = balanceTransactionRepository;
    }

    public List<SkinOfferResponse> firstBuy(BigDecimal amount) {
        List<BigDecimal> shares = List.of(
                new BigDecimal("0.25"),
                new BigDecimal("0.20"),
                new BigDecimal("0.18"),
                new BigDecimal("0.15"),
                new BigDecimal("0.12")
        );

        return shares.stream()
                .map(share -> findSkinForBudget(amount.multiply(share)))
                .toList();
    }

    @Transactional
    public SkinOfferResponse buy(UUID userId, String listingId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        var skin = marketService.findByListingId(listingId); // RUB

        if (user.getBalance().compareTo(skin.price()) < 0) {
            throw new InsufficientBalanceException();
        }

        user.withdraw(skin.price());

        inventoryRepository.save(
                new InventoryItem(
                        userId,
                        skin.listingId(),
                        skin.marketHashName(),
                        skin.imageUrl(),
                        skin.price(), // RUB
                        skin.rarity(),
                        skin.wear()
                )
        );

        balanceTransactionRepository.save(
                BalanceTransaction.skinBuy(userId, skin.price(), skin.marketHashName(), skin.imageUrl()));

        return skin;
    }

    private SkinOfferResponse findSkinForBudget(BigDecimal budget) {
        BigDecimal minPrice = budget.multiply(new BigDecimal("0.90"));

        var page = marketService.find(
                minPrice,
                budget,
                20,
                PriceSort.DESC,
                null
        );

        if (page.items().isEmpty()) {
            throw new IllegalStateException(
                    "No skin found for budget: " + budget
            );
        }

        return page.items().getFirst();
    }
}
