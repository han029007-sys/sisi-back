package com.skinforge.upgrader.repository;

import com.skinforge.upgrader.model.BalanceTransaction;
import com.skinforge.upgrader.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Long> {
    List<BalanceTransaction> findByUserId(String userId);

    Page<BalanceTransaction> findByUserIdOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );
}
