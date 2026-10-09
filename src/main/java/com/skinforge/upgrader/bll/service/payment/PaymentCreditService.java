package com.skinforge.upgrader.bll.service.payment;

import com.skinforge.upgrader.bll.service.skin.SkinPurchaseService;
import com.skinforge.upgrader.bll.service.skin.dto.SkinOfferResponse;
import com.skinforge.upgrader.model.*;
import com.skinforge.upgrader.repository.BalanceTransactionRepository;
import com.skinforge.upgrader.repository.InventoryRepository;
import com.skinforge.upgrader.repository.PaymentRepository;
import com.skinforge.upgrader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCreditService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BalanceTransactionRepository balanceTransactionRepository;
    private final SkinPurchaseService skinPurchaseService;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public void credit(UUID paymentId, String invoiceId, String custom, BigDecimal amount) {

        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElseThrow();

        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not pending: " + paymentId);
        }

        if (!invoiceId.equals(payment.getInvoiceId())) {
            throw new IllegalStateException("Invoice mismatch");
        }

        if (!payment.getId().toString().equals(custom)) {
            throw new IllegalStateException("Custom ID mismatch");
        }

        if (amount.compareTo(payment.getAmount()) != 0) {
            throw new IllegalStateException("Amount mismatch");
        }

        User user = userRepository.findByIdForUpdate(payment.getUserId()).orElseThrow(() -> new IllegalStateException("Payment user not found"));

        user.deposit(payment.getAmount());

        balanceTransactionRepository.save(BalanceTransaction.deposit(user.getId(), payment.getAmount()));

        if (!user.isFirstDepositCompleted()) {

            var skins = skinPurchaseService.firstBuy(payment.getAmount());

            var inventoryItems = skins.stream()
                    .map(skin -> new InventoryItem(
                            user.getId(),
                            skin.listingId(),
                            skin.marketHashName(),
                            skin.imageUrl(),
                            skin.price(),
                            skin.rarity(),
                            skin.wear()))
                    .toList();

            BigDecimal totalPrice = skins.stream()
                    .map(SkinOfferResponse::price)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalPrice.compareTo(payment.getAmount()) > 0) {
                throw new IllegalStateException("First purchase exceeds deposit amount");
            }

            var transactions = skins.stream()
                    .map(skin -> BalanceTransaction.skinBuy(
                            user.getId(),
                            skin.price(),
                            skin.marketHashName(),
                            skin.imageUrl()))
                    .toList();

            user.withdraw(totalPrice);

            inventoryRepository.saveAll(inventoryItems);
            balanceTransactionRepository.saveAll(transactions);

            user.setFirstDepositCompleted();

            log.info("First deposit completed: user={}, skins={}, total={}", user.getId(), skins.size(), totalPrice);
        }

        payment.markPaid();

        log.info("Payment {} credited: user={}, amount={} RUB", paymentId, user.getId(), payment.getAmount());
    }

    @Transactional
    public void markFailed(UUID paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElseThrow();
        payment.markFailed();
    }

    @Transactional
    public void markExpired(UUID paymentId) {
        Payment payment = paymentRepository.findByIdForUpdate(paymentId).orElseThrow();
        payment.markExpired();
    }
}