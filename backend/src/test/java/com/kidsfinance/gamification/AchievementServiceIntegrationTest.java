package com.kidsfinance.gamification;

import com.kidsfinance.challenge.ChallengeService;
import com.kidsfinance.challenge.dto.request.ChallengeAnswerRequest;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import com.kidsfinance.simulation.SimulationService;
import com.kidsfinance.simulation.dto.request.SimulationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AchievementServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(
                    "postgres:17-alpine"
            );

    @Autowired
    private ParentService parentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private SimulationService simulationService;

    @Test
    void newChildShouldStartWithLockedAchievements() {

        ChildResponse child =
                createChild(
                        "achievement_parent_one",
                        "achievement_child_one",
                        "achievement.one@example.com"
                );

        var achievements =
                achievementService
                        .getAchievementsForChild(
                                child.userId()
                        );

        assertThat(achievements)
                .hasSize(4);

        assertThat(achievements)
                .allSatisfy(achievement ->
                        assertThat(
                                achievement.earned()
                        ).isFalse()
                );
    }

    @Test
    void correctChallengeShouldAwardFirstChallengeAchievement() {

        ChildResponse child =
                createChild(
                        "achievement_parent_two",
                        "achievement_child_two",
                        "achievement.two@example.com"
                );

        challengeService.submitAnswerForChild(
                child.userId(),
                "SAVING_01",
                new ChallengeAnswerRequest(
                        "SAVE_FIRST"
                )
        );

        var achievements =
                achievementService
                        .getAchievementsForChild(
                                child.userId()
                        );

        var firstChallenge =
                achievements.stream()
                        .filter(achievement ->
                                achievement.code()
                                        .equals(
                                                "FIRST_CHALLENGE"
                                        )
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(firstChallenge.earned())
                .isTrue();

        assertThat(firstChallenge.earnedAt())
                .isNotNull();
    }

    @Test
    void balancedSimulationShouldAwardSimulationAndBalancedAchievements() {

        ChildResponse child =
                createChild(
                        "achievement_parent_three",
                        "achievement_child_three",
                        "achievement.three@example.com"
                );

        simulationService.runForChild(
                child.userId(),
                new SimulationRequest(
                        amount("100"),
                        amount("200"),
                        amount("200"),
                        amount("100"),
                        amount("150"),
                        amount("100"),
                        amount("150")
                )
        );

        var achievements =
                achievementService
                        .getAchievementsForChild(
                                child.userId()
                        );

        assertThat(achievements)
                .filteredOn(achievement ->
                        achievement.code()
                                .equals(
                                        "FIRST_SIMULATION"
                                )
                )
                .allSatisfy(achievement ->
                        assertThat(
                                achievement.earned()
                        ).isTrue()
                );

        assertThat(achievements)
                .filteredOn(achievement ->
                        achievement.code()
                                .equals(
                                        "BALANCED_50"
                                )
                )
                .allSatisfy(achievement ->
                        assertThat(
                                achievement.earned()
                        ).isTrue()
                );
    }

    private ChildResponse createChild(
            String parentUsername,
            String childUsername,
            String email
    ) {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                parentUsername,
                                "Password123!",
                                "Achievement Parent",
                                email
                        )
                );

        return childService
                .createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                childUsername,
                                "Password123!",
                                "Achievement Child",
                                (short) 12
                        )
                );
    }

    private BigDecimal amount(
            String value
    ) {
        return new BigDecimal(value);
    }
}