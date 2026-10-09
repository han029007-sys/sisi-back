package com.skinforge.upgrader.bll.service.payment;

import com.skinforge.upgrader.integration.payment.coinso.CryptoProcClient;
import com.skinforge.upgrader.model.Payment;
import com.skinforge.upgrader.model.PaymentStatus;
import com.skinforge.upgrader.repository.BalanceTransactionRepository;
import com.skinforge.upgrader.repository.PaymentRepository;
import com.skinforge.upgrader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationService {

    private final PaymentRepository paymentRepository;
    private final CryptoProcClient cryptoProcClient;
    private final PaymentCreditService paymentCreditService;

    public void confirm(String invoiceId) {

        if (invoiceId == null || invoiceId.isBlank()) {
            throw new IllegalArgumentException("Invoice ID is required");
        }

        Payment payment = paymentRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoice: " + invoiceId));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        var status = cryptoProcClient.getStatus(invoiceId);

        if (status == null || !status.success()) {
            throw new IllegalStateException("Could not verify CryptoProc payment");
        }

        if (!invoiceId.equals(status.invoice_id())) {
            throw new IllegalStateException("Invoice ID mismatch");
        }

        if (!payment.getId().toString().equals(status.custom())) {
            throw new IllegalStateException("Payment custom ID mismatch");
        }

        BigDecimal confirmedAmount = status.amount();

        if (confirmedAmount == null || confirmedAmount.compareTo(payment.getAmount()) != 0) {
            throw new IllegalStateException("Payment amount mismatch");
        }

        switch (status.status()) {
            case "paid" -> paymentCreditService.credit(payment.getId(), invoiceId, status.custom(), confirmedAmount);
            case "failed" -> paymentCreditService.markFailed(payment.getId());
            case "expired" -> paymentCreditService.markExpired(payment.getId());
            case "pending" -> log.debug("Payment {} is still pending", payment.getId());
            default -> throw new IllegalStateException("Unknown CryptoProc payment status: " + status.status());
        }
    }
}
