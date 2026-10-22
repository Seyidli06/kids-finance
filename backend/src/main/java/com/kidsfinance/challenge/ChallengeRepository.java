package com.kidsfinance.challenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository
        extends JpaRepository<Challenge, Long> {

    List<Challenge>
    findAllByActiveTrueOrderByDisplayOrderAsc();

    Optional<Challenge>
    findByCodeAndActiveTrue(String code);
}