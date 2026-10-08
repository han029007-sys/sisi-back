package com.skinforge.upgrader.repository;

import com.skinforge.upgrader.model.InventoryItem;
import com.skinforge.upgrader.model.InventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByUserId(UUID userId);

    List<InventoryItem> findByUserIdAndStatus(
            UUID userId,
            InventoryStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select i
        from InventoryItem i
        where i.id = :id
          and i.userId = :userId
    """)
    Optional<InventoryItem> findByIdAndUserIdForUpdate(
            Long id,
            UUID userId
    );
}
