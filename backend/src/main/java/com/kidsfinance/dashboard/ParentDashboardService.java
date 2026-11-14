package com.kidsfinance.dashboard;

import com.kidsfinance.challenge.ChallengeAttemptRepository;
import com.kidsfinance.dashboard.dto.response.ParentDashboardResponse;
import com.kidsfinance.gamification.AchievementService;
import com.kidsfinance.gamification.StreakService;
import com.kidsfinance.gamification.dto.response.AchievementResponse;
import com.kidsfinance.gamification.dto.response.StreakResponse;
import com.kidsfinance.scoring.ProgressService;
import com.kidsfinance.scoring.SkillType;
import com.kidsfinance.scoring.dto.response.ChildProgressResponse;
import com.kidsfinance.scoring.dto.response.SkillScoreResponse;
import com.kidsfinance.simulation.SimulationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentDashboardService {

    private static final int
            NEEDS_IMPROVEMENT_THRESHOLD = 60;

    private final ProgressService progressService;
    private final StreakService streakService;
    private final AchievementService achievementService;

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final SimulationRunRepository simulationRunRepository;

    @Transactional(readOnly = true)
    public ParentDashboardResponse getDashboard(
            Long parentUserId,
            Long childProfileId
    ) {

        /*
         * ProgressService performs parent-child ownership
         * validation before returning any child data.
         */
        ChildProgressResponse progress =
                progressService.getProgressForParent(
                        parentUserId,
                        childProfileId
                );

        StreakResponse streak =
                streakService.getForParent(
                        parentUserId,
                        childProfileId
                );

        List<AchievementResponse> achievements =
                achievementService
                        .getAchievementsForParent(
                                parentUserId,
                                childProfileId
                        );

        long completedChallenges =
                challengeAttemptRepository
                        .countCompletedChallenges(
                                childProfileId
                        );

        long completedSimulations =
                simulationRunRepository
                        .countByChild_Id(
                                childProfileId
                        );

        long completedActivities =
                Math.addExact(
                        completedChallenges,
                        completedSimulations
                );

        long earnedAchievements =
                achievements.stream()
                        .filter(
                                AchievementResponse::earned
                        )
                        .count();

        List<SkillType> areasForImprovement =
                findAreasForImprovement(
                        progress.skillScores()
                );

        return new ParentDashboardResponse(
                progress.childId(),
                progress.displayName(),

                progress.xp(),
                progress.level(),

                progress.xpInCurrentLevel(),
                progress.xpToNextLevel(),

                progress.overallScore(),

                progress.skillScores(),

                streak,

                completedChallenges,
                completedSimulations,
                completedActivities,

                earnedAchievements,

                areasForImprovement
        );
    }

    private List<SkillType> findAreasForImprovement(
            List<SkillScoreResponse> skillScores
    ) {

        /*
         * Unattempted skills are not labelled as weak yet.
         * We only evaluate skills with actual activity data.
         */
        return skillScores.stream()
                .filter(score ->
                        score.activityCount() > 0
                )
                .filter(score ->
                        score.score()
                                < NEEDS_IMPROVEMENT_THRESHOLD
                )
                .sorted(
                        Comparator.comparingInt(
                                SkillScoreResponse::score
                        )
                )
                .map(
                        SkillScoreResponse::skill
                )
                .toList();
    }
}