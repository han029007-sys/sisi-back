package com.skinforge.upgrader.repository;

import com.skinforge.upgrader.model.Payment;
import com.skinforge.upgrader.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByInvoiceId(String invoiceId);

    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByIdForUpdate(UUID id);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findTop100ByStatusAndInvoiceIdIsNotNullOrderByCreatedAtAsc(
            PaymentStatus status
    );
}