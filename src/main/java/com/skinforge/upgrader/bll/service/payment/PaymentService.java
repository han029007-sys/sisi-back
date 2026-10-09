package com.skinforge.upgrader.bll.service.payment;

import com.skinforge.upgrader.bll.service.payment.dto.CreatePaymentRequest;
import com.skinforge.upgrader.bll.service.payment.dto.CreatePaymentResponse;
import com.skinforge.upgrader.integration.payment.coinso.CryptoProcClient;
import com.skinforge.upgrader.model.Payment;
import com.skinforge.upgrader.repository.PaymentRepository;
import com.skinforge.upgrader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final CryptoProcClient cryptoProcClient;
    private final TransactionTemplate transactionTemplate;

    private final BigDecimal MINIMAL_AMOUNT = new BigDecimal("100");

    public CreatePaymentResponse createPayment(
            UUID userId,
            CreatePaymentRequest request
    ) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException(
                    "User not found"
            );
        }
        if (request.amount().compareTo(MINIMAL_AMOUNT) < 0){
            throw new IllegalArgumentException("Amount less than 100");
        }

        BigDecimal amount = request.amount()
                .setScale(2, RoundingMode.UNNECESSARY);

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        Payment payment = transactionTemplate.execute(status ->
                paymentRepository.save(
                        new Payment(userId, amount)
                )
        );

        if (payment == null) {
            throw new IllegalStateException(
                    "Could not create payment"
            );
        }

        CryptoProcClient.CreateInvoiceResponse invoice =
                cryptoProcClient.createInvoice(
                        amount,
                        payment.getId().toString(),
                        request.email()
                );

        if (invoice == null
                || !invoice.success()
                || invoice.invoice_id() == null
                || invoice.invoice_id().isBlank()
                || invoice.payment_url() == null
                || invoice.payment_url().isBlank()) {

            throw new IllegalStateException(
                    "CryptoProc did not return a valid invoice"
            );
        }

        transactionTemplate.executeWithoutResult(status -> {
            Payment savedPayment = paymentRepository
                    .findByIdForUpdate(payment.getId())
                    .orElseThrow();

            savedPayment.setInvoiceId(invoice.invoice_id());
        });

        return new CreatePaymentResponse(
                payment.getId(),
                invoice.invoice_id(),
                amount,
                invoice.payment_url(),
                "PENDING"
        );
    }
}