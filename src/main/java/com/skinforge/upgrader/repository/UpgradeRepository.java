package com.skinforge.upgrader.repository;

import com.skinforge.upgrader.model.Upgrade;
import com.skinforge.upgrader.model.UpgradeResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UpgradeRepository extends JpaRepository<Upgrade, Long> {
    Page<Upgrade> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    long countByUserIdAndResult(
            UUID userId,
            UpgradeResult result
    );

    Optional<Upgrade> findTopByUserIdAndResultOrderByTargetPriceDesc(
            UUID userId,
            UpgradeResult result
    );

    @Query("""
                select coalesce(sum(
                    case
                        when u.result = com.skinforge.upgrader.model.UpgradeResult.LOSE
                            then u.inputPrice
                        when u.result = com.skinforge.upgrader.model.UpgradeResult.WIN
                            then u.inputPrice - u.targetPrice
                        else 0
                    end
                ), 0)
                from Upgrade u
                where u.userId = :userId
            """)
    BigDecimal calculateUserPnl(UUID userId);
}
