package com.kidsfinance.gamification;

import com.kidsfinance.challenge.ChallengeAttemptRepository;
import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.gamification.dto.response.AchievementResponse;
import com.kidsfinance.scoring.ChildSkillScore;
import com.kidsfinance.scoring.ChildSkillScoreRepository;
import com.kidsfinance.scoring.SkillType;
import com.kidsfinance.simulation.SimulationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final ChildAchievementRepository childAchievementRepository;

    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final SimulationRunRepository simulationRunRepository;
    private final ChildSkillScoreRepository childSkillScoreRepository;

    private final ChildProfileRepository childProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;

    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public void evaluateAchievements(
            ChildProfile child
    ) {

        List<Achievement> achievements =
                achievementRepository
                        .findAllByActiveTrueOrderByDisplayOrderAsc();

        for (Achievement achievement : achievements) {

            boolean alreadyEarned =
                    childAchievementRepository
                            .existsByChild_IdAndAchievement_Id(
                                    child.getId(),
                                    achievement.getId()
                            );

            if (alreadyEarned) {
                continue;
            }

            if (!meetsCondition(
                    child,
                    achievement
            )) {
                continue;
            }

            ChildAchievement earned =
                    ChildAchievement.builder()
                            .child(child)
                            .achievement(achievement)
                            .build();

            childAchievementRepository.save(
                    earned
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse>
    getAchievementsForChild(
            Long childUserId
    ) {

        ChildProfile child =
                childProfileRepository
                        .findByUser_Id(childUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Child profile not found for authenticated user"
                                )
                        );

        return getAchievements(
                child.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse>
    getAchievementsForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        boolean belongsToParent =
                parentChildLinkRepository
                        .existsByParent_User_IdAndChild_Id(
                                parentUserId,
                                childProfileId
                        );

        if (!belongsToParent) {
            throw new ResourceNotFoundException(
                    "Child not found for authenticated parent"
            );
        }

        return getAchievements(
                childProfileId
        );
    }

    private boolean meetsCondition(
            ChildProfile child,
            Achievement achievement
    ) {

        return switch (
                achievement.getConditionType()
                ) {

            case FIRST_CORRECT_CHALLENGE ->
                    challengeAttemptRepository
                            .existsByChild_IdAndCorrectTrue(
                                    child.getId()
                            );

            case FIRST_SIMULATION ->
                    simulationRunRepository
                            .existsByChild_Id(
                                    child.getId()
                            );

            case XP_AT_LEAST ->
                    child.getXp()
                            >= achievement.getThreshold();

            case ALL_SKILLS_AT_LEAST ->
                    allSkillsAtLeast(
                            child.getId(),
                            achievement.getThreshold()
                    );
        };
    }

    private boolean allSkillsAtLeast(
            Long childProfileId,
            int threshold
    ) {

        List<ChildSkillScore> scores =
                childSkillScoreRepository
                        .findAllByChild_Id(
                                childProfileId
                        );

        if (scores.size()
                != SkillType.values().length) {

            return false;
        }

        return scores.stream()
                .allMatch(score ->
                        score.getScore()
                                >= threshold
                );
    }

    private List<AchievementResponse>
    getAchievements(
            Long childProfileId
    ) {

        Map<Long, OffsetDateTime> earnedAt =
                childAchievementRepository
                        .findAllByChild_Id(
                                childProfileId
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        childAchievement ->
                                                childAchievement
                                                        .getAchievement()
                                                        .getId(),
                                        ChildAchievement::getEarnedAt
                                )
                        );

        return achievementRepository
                .findAllByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(achievement -> {

                    OffsetDateTime earned =
                            earnedAt.get(
                                    achievement.getId()
                            );

                    return new AchievementResponse(
                            achievement.getCode(),
                            achievement.getTitle(),
                            achievement.getDescription(),
                            earned != null,
                            earned
                    );
                })
                .toList();
    }
}