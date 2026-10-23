package com.kidsfinance.challenge;

import com.kidsfinance.challenge.dto.request.ChallengeAnswerRequest;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import com.kidsfinance.scoring.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ChallengeServiceIntegrationTest {

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
    void shouldLoadMvpChallengeCatalog() {

        ChildResponse child =
                createChild(
                        "challenge_catalog_parent",
                        "challenge_catalog_child",
                        "challenge.catalog@example.com"
                );

        var challenges =
                challengeService
                        .getChallengesForChild(
                                child.userId()
                        );

        assertThat(challenges)
                .hasSize(5);

        assertThat(challenges)
                .extracting(
                        challenge ->
                                challenge.topicCode()
                )
                .containsExactly(
                        "SAVING",
                        "BUDGETING",
                        "SPENDING",
                        "INVESTING",
                        "INVESTING"
                );

        assertThat(challenges.getFirst().options())
                .isNotEmpty();
    }

    @Test
    void shouldAwardXpOnlyForFirstCorrectCompletion() {

        ChildResponse child =
                createChild(
                        "challenge_xp_parent",
                        "challenge_xp_child",
                        "challenge.xp@example.com"
                );

        var wrongAttempt =
                challengeService
                        .submitAnswerForChild(
                                child.userId(),
                                "SAVING_01",
                                new ChallengeAnswerRequest(
                                        "SPEND_FIRST"
                                )
                        );

        assertThat(wrongAttempt.correct())
                .isFalse();

        assertThat(wrongAttempt.xpAwarded())
                .isZero();

        var firstCorrectAttempt =
                challengeService
                        .submitAnswerForChild(
                                child.userId(),
                                "SAVING_01",
                                new ChallengeAnswerRequest(
                                        "SAVE_FIRST"
                                )
                        );

        assertThat(firstCorrectAttempt.correct())
                .isTrue();

        assertThat(firstCorrectAttempt.xpAwarded())
                .isEqualTo(10);

        var replayAttempt =
                challengeService
                        .submitAnswerForChild(
                                child.userId(),
                                "SAVING_01",
                                new ChallengeAnswerRequest(
                                        "SAVE_FIRST"
                                )
                        );

        assertThat(replayAttempt.correct())
                .isTrue();

        assertThat(replayAttempt.xpAwarded())
                .isZero();

        var progress =
                progressService
                        .getProgressForChild(
                                child.userId()
                        );

        assertThat(progress.xp())
                .isEqualTo(10);

        var challenges =
                challengeService
                        .getChallengesForChild(
                                child.userId()
                        );

        var savingChallenge =
                challenges.stream()
                        .filter(challenge ->
                                challenge.code()
                                        .equals("SAVING_01")
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(savingChallenge.completed())
                .isTrue();
    }

    @Test
    void shouldRejectUnknownChallengeOption() {

        ChildResponse child =
                createChild(
                        "challenge_invalid_parent",
                        "challenge_invalid_child",
                        "challenge.invalid@example.com"
                );

        assertThatThrownBy(() ->
                challengeService
                        .submitAnswerForChild(
                                child.userId(),
                                "SAVING_01",
                                new ChallengeAnswerRequest(
                                        "NOT_REAL_OPTION"
                                )
                        )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Challenge option not found"
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
                                "Challenge Parent",
                                email
                        )
                );

        return childService
                .createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                childUsername,
                                "Password123!",
                                "Challenge Child",
                                (short) 12
                        )
                );
    }
}