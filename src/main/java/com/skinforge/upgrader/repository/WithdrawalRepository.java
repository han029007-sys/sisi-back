package com.skinforge.upgrader.repository;

import com.skinforge.upgrader.model.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WithdrawalRepository
        extends JpaRepository<Withdrawal, Long> {

    List<Withdrawal> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Withdrawal> findByProjectId(String projectId);
}