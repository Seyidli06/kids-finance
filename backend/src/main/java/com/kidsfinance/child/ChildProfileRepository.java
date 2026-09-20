package com.kidsfinance.child;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChildProfileRepository
        extends JpaRepository<ChildProfile, Long> {

    Optional<ChildProfile> findByUser_Id(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM ChildProfile c
            WHERE c.id = :childProfileId
            """)
    Optional<ChildProfile> findByIdForUpdate(
            @Param("childProfileId") Long childProfileId
    );
}