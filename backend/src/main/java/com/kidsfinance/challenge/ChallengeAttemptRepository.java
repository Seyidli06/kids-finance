package com.kidsfinance.challenge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeAttemptRepository
        extends JpaRepository<ChallengeAttempt, Long> {

    boolean existsByChild_IdAndChallenge_IdAndCorrectTrue(
            Long childProfileId,
            Long challengeId
    );

    List<ChallengeAttempt>
    findAllByChild_IdOrderByCreatedAtDescIdDesc(
            Long childProfileId
    );
}