package com.skinforge.upgrader.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor
public class Payment {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Setter
    @Column(name = "invoice_id", unique = true)
    private String invoiceId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment(UUID userId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive"
            );
        }

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.amount = amount.setScale(2);
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markPaid() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Payment is not pending"
            );
        }

        status = PaymentStatus.PAID;
        paidAt = LocalDateTime.now();
    }

    public void markFailed() {
        if (status == PaymentStatus.PENDING) {
            status = PaymentStatus.FAILED;
        }
    }

    public void markExpired() {
        if (status == PaymentStatus.PENDING) {
            status = PaymentStatus.EXPIRED;
        }
    }
}