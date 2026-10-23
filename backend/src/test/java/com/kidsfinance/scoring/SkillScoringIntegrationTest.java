package com.kidsfinance.scoring;

import com.kidsfinance.challenge.ChallengeService;
import com.kidsfinance.challenge.dto.request.ChallengeAnswerRequest;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SkillScoringIntegrationTest {

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
    private ChallengeService challengeService;

    @Autowired
    private ProgressService progressService;

    @Test
    void shouldInitializeAllFiveSkillScores() {

        ChildResponse child =
                createChild(
                        "skills_parent",
                        "skills_child",
                        "skills@example.com"
                );

        var progress =
                progressService
                        .getProgressForChild(
                                child.userId()
                        );

        assertThat(progress.overallScore())
                .isZero();

        assertThat(progress.skillScores())
                .hasSize(5);

        assertThat(progress.skillScores())
                .allSatisfy(score -> {
                    assertThat(score.score())
                            .isZero();

                    assertThat(score.activityCount())
                            .isZero();
                });

        assertThat(progress.skillScores())
                .extracting(score -> score.skill())
                .containsExactly(
                        SkillType.SAVING,
                        SkillType.BUDGETING,
                        SkillType.RISK_MANAGEMENT,
                        SkillType.INVESTMENT,
                        SkillType.DECISION_MAKING
                );
    }

    @Test
    void shouldUpdateSkillScoreUsingActivityResults() {

        ChildResponse child =
                createChild(
                        "score_parent",
                        "score_child",
                        "score@example.com"
                );

        challengeService.submitAnswerForChild(
                child.userId(),
                "SAVING_01",
                new ChallengeAnswerRequest(
                        "SPEND_FIRST"
                )
        );

        challengeService.submitAnswerForChild(
                child.userId(),
                "SAVING_01",
                new ChallengeAnswerRequest(
                        "SAVE_FIRST"
                )
        );

        challengeService.submitAnswerForChild(
                child.userId(),
                "SAVING_01",
                new ChallengeAnswerRequest(
                        "SAVE_FIRST"
                )
        );

        var progress =
                progressService
                        .getProgressForChild(
                                child.userId()
                        );

        var saving =
                progress.skillScores()
                        .stream()
                        .filter(score ->
                                score.skill()
                                        == SkillType.SAVING
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(saving.score())
                .isEqualTo(67);

        assertThat(saving.activityCount())
                .isEqualTo(3);

        assertThat(progress.xp())
                .isEqualTo(10);
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
                                "Score Parent",
                                email
                        )
                );

        return childService
                .createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                childUsername,
                                "Password123!",
                                "Score Child",
                                (short) 12
                        )
                );
    }
}