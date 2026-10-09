package com.skinforge.upgrader.bll.service.payment;

import com.skinforge.upgrader.model.PaymentStatus;
import com.skinforge.upgrader.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationJob {

    private final PaymentRepository paymentRepository;
    private final PaymentConfirmationService confirmationService;

    @Scheduled(fixedDelayString =
            "${cryptoproc.reconciliation-delay-ms:60000}")
    public void reconcile() {

        var payments = paymentRepository
                .findTop100ByStatusAndInvoiceIdIsNotNullOrderByCreatedAtAsc(
                        PaymentStatus.PENDING
                );

        for (var payment : payments) {

            try {
                confirmationService.confirm(payment.getInvoiceId());

            } catch (Exception e) {
                log.error(
                        "Failed to reconcile payment {}",
                        payment.getId(),
                        e
                );
            }
        }
    }
}