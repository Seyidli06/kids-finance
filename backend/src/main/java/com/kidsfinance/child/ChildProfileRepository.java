package com.kidsfinance.child;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChildProfileRepository
        extends JpaRepository<ChildProfile, Long> {

    Optional<ChildProfile> findByUser_Id(Long userId);
}