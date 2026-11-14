package com.kidsfinance.challenge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChallengeAttemptRepository
        extends JpaRepository<ChallengeAttempt, Long> {

    boolean existsByChild_IdAndChallenge_IdAndCorrectTrue(
            Long childProfileId,
            Long challengeId
    );

    boolean existsByChild_IdAndCorrectTrue(
            Long childProfileId
    );

    @Query("""
            SELECT COUNT(DISTINCT attempt.challenge.id)
            FROM ChallengeAttempt attempt
            WHERE attempt.child.id = :childProfileId
              AND attempt.correct = true
            """)
    long countCompletedChallenges(
            @Param("childProfileId")
            Long childProfileId
    );

    List<ChallengeAttempt>
    findAllByChild_IdOrderByCreatedAtDescIdDesc(
            Long childProfileId
    );
}