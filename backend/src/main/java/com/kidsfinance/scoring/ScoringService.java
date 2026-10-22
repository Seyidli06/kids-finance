package com.kidsfinance.scoring;

import com.kidsfinance.child.ChildProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoringService {

    private static final int SCENARIO_COMPLETION_XP = 10;

    public static final int XP_PER_LEVEL = 100;

    @Transactional(propagation = Propagation.MANDATORY)
    public int awardScenarioCompletionXp(
            ChildProfile child
    ) {
        return awardXp(
                child,
                SCENARIO_COMPLETION_XP
        );
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public int awardChallengeCompletionXp(
            ChildProfile child,
            int xpReward
    ) {
        return awardXp(
                child,
                xpReward
        );
    }

    private int awardXp(
            ChildProfile child,
            int amount
    ) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "XP amount cannot be negative"
            );
        }

        int newXp = Math.addExact(
                child.getXp(),
                amount
        );

        int newLevel =
                (newXp / XP_PER_LEVEL) + 1;

        child.setXp(newXp);
        child.setLevel(newLevel);

        return amount;
    }
}