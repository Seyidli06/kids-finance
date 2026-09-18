package com.kidsfinance.parent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentProfileRepository
        extends JpaRepository<ParentProfile, Long> {

    Optional<ParentProfile> findByUser_Id(Long userId);

    Optional<ParentProfile> findByEmail(String email);

    boolean existsByEmail(String email);
}