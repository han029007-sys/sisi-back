package com.skinforge.upgrader.bll.service.withdraw;

import com.skinforge.upgrader.bll.service.withdraw.dto.WithdrawalResponse;
import com.skinforge.upgrader.exception.InventoryNotFoundException;
import com.skinforge.upgrader.exception.UserNotFoundException;
import com.skinforge.upgrader.model.InventoryStatus;
import com.skinforge.upgrader.model.Withdrawal;
import com.skinforge.upgrader.provider.lisskins.LisPurchaseService;
import com.skinforge.upgrader.repository.InventoryRepository;
import com.skinforge.upgrader.repository.UserRepository;
import com.skinforge.upgrader.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final LisPurchaseService lisPurchaseService;

    @Transactional
    public WithdrawalResponse withdraw(UUID userId, Long inventoryId) {

        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getTradeUrl() == null || user.getTradeUrl().isBlank()) {

            throw new IllegalStateException("Steam trade URL is not configured");
        }

        var item = inventoryRepository.findById(inventoryId).orElseThrow(()
                -> new InventoryNotFoundException("Inventory item not found: " + inventoryId));

        if (!item.getUserId().equals(userId)) {

            throw new IllegalStateException("Skin does not belong to user");
        }

        if (item.getStatus() != InventoryStatus.AVAILABLE) {

            throw new IllegalStateException("Skin is not available");
        }

        String projectId = "withdraw-" + UUID.randomUUID();

        /*
         * Пока listingId ещё неизвестен.
         * LIS PurchaseService сам найдёт свежий.
         */
        Withdrawal withdrawal = new Withdrawal(userId, item.getId(), projectId, item.getMarketHashName(), null, item.getPrice());

        withdrawalRepository.save(withdrawal);

        item.markWithdrawPending();

        try {

            var result = lisPurchaseService.buy(user.getTradeUrl(), item.getMarketHashName(), projectId);

            /*
             * Сейчас в Withdrawal нет setter-а
             * providerListingId.
             *
             * Чуть ниже добавим метод.
             */

            withdrawal.setProviderData(String.valueOf(result.listingId()), result.price());

            withdrawal.markProcessing();

        } catch (Exception e) {

            item.markAvailable();

            withdrawal.markFailed();

            throw e;
        }

        return new WithdrawalResponse(
                withdrawal.getId(), item.getId(), withdrawal.getProjectId(), withdrawal.getStatus());
    }

    @Transactional
    public WithdrawalResponse checkStatus(UUID userId, String projectId) {

        var withdrawal = withdrawalRepository.findByProjectId(projectId)
                .orElseThrow(()
                -> new IllegalStateException("Withdrawal not found: " + projectId));

        if (!withdrawal.getUserId().equals(userId)) {

            throw new IllegalStateException("Withdrawal does not belong to user");
        }

        var item = inventoryRepository.findById(withdrawal.getInventoryId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item not found"));

        var trade = lisPurchaseService.getStatus(projectId);

        String status = trade.status();

        /*
         * Из документации точно знаем:
         *
         * processing
         * wait_accept
         * wait_unlock
         * wait_withdraw
         * return
         */

        switch (status) {

            case "processing", "wait_accept", "wait_unlock", "wait_withdraw" -> {

                // всё ещё PROCESSING
            }

            case "return" -> {

                withdrawal.markFailed();

                item.markAvailable();
            }

            default -> {

                /*
                 * Финальное имя успешного статуса
                 * лучше подтвердить по реальному
                 * LIS response/WebSocket.
                 *
                 * Пока неизвестный статус не считаем SUCCESS.
                 */
            }
        }

        return new WithdrawalResponse(
                withdrawal.getId(), item.getId(), withdrawal.getProjectId(), withdrawal.getStatus());
    }
}