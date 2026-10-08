package com.skinforge.upgrader.bll.service.balance;

import com.skinforge.upgrader.bll.service.balance.dto.BalanceTransactionResponse;
import com.skinforge.upgrader.bll.service.skin.SkinPurchaseService;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.exception.InvalidAmountException;
import com.skinforge.upgrader.exception.UserNotFoundException;
import com.skinforge.upgrader.model.BalanceTransaction;
import com.skinforge.upgrader.model.InventoryItem;
import com.skinforge.upgrader.model.User;
import com.skinforge.upgrader.repository.BalanceTransactionRepository;
import com.skinforge.upgrader.repository.InventoryRepository;
import com.skinforge.upgrader.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceService {
    private final UserRepository userRepository;
    private final SkinPurchaseService skinPurchaseService;
    private final InventoryRepository inventoryRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;

    private final BigDecimal MINIMAL_AMOUNT = new BigDecimal("50");

    public BalanceService(UserRepository userRepository, SkinPurchaseService skinPurchaseService, InventoryRepository inventoryRepository, BalanceTransactionRepository balanceTransactionRepository) {
        this.userRepository = userRepository;
        this.skinPurchaseService = skinPurchaseService;
        this.inventoryRepository = inventoryRepository;
        this.balanceTransactionRepository = balanceTransactionRepository;
    }

    @Transactional
    public void depositTest(UUID userId, BigDecimal amount){
        if (amount.compareTo(MINIMAL_AMOUNT) < 0){
            throw new InvalidAmountException(amount);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.deposit(amount);

        BalanceTransaction balanceTransaction = BalanceTransaction.deposit(userId, amount);
        balanceTransactionRepository.save(balanceTransaction);

        if(user.isFirstDepositCompleted()){
            return;
        }

        var skins = skinPurchaseService.firstBuy(amount);
        var inventoryItems = skins.stream()
                .map(skin -> new InventoryItem(
                        user.getId(),
                        skin.listingId(),
                        skin.marketHashName(),
                        skin.imageUrl(),
                        skin.price(),
                        skin.rarity(),
                        skin.wear()
                ))
                .toList();

        var transactions = skins.stream()
                .map(skin -> BalanceTransaction.skinBuy(
                        userId, skin.price(), skin.marketHashName(), skin.imageUrl()))
                .toList();

        BigDecimal totalPrice = skins.stream()
                .map(SkinOfferResponse::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        user.withdraw(totalPrice);

        inventoryRepository.saveAll(inventoryItems);
        balanceTransactionRepository.saveAll(transactions);

        user.setFirstDepositCompleted();
    }

    @Transactional(readOnly = true)
    public Page<BalanceTransactionResponse> getTransactions(
            UUID userId,
            Pageable pageable
    ) {
        return balanceTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(transaction -> new BalanceTransactionResponse(
                        transaction.getId(),
                        transaction.getType(),
                        transaction.getAmount(),
                        transaction.getSkinName(),
                        transaction.getSkinImageUrl(),
                        transaction.getCreatedAt()
                ));
    }
}
