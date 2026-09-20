package com.kidsfinance.scoring;

import com.kidsfinance.child.ChildProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoringService {

    private static final int SCENARIO_COMPLETION_XP = 10;
    private static final int XP_PER_LEVEL = 100;

    @Transactional(propagation = Propagation.MANDATORY)
    public int awardScenarioCompletionXp(ChildProfile child) {

        int newXp = Math.addExact(
                child.getXp(),
                SCENARIO_COMPLETION_XP
        );

        int newLevel = (newXp / XP_PER_LEVEL) + 1;

        child.setXp(newXp);
        child.setLevel(newLevel);

        return SCENARIO_COMPLETION_XP;
    }
}