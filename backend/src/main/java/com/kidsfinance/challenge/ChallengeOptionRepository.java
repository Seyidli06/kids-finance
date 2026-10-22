package com.kidsfinance.challenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeOptionRepository
        extends JpaRepository<ChallengeOption, Long> {

    List<ChallengeOption>
    findAllByChallenge_IdAndActiveTrueOrderByDisplayOrderAsc(
            Long challengeId
    );

    Optional<ChallengeOption>
    findByChallenge_IdAndCodeAndActiveTrue(
            Long challengeId,
            String code
    );
}