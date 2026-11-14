package com.kidsfinance.dashboard;

import com.kidsfinance.challenge.ChallengeService;
import com.kidsfinance.challenge.dto.request.ChallengeAnswerRequest;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.common.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ParentDashboardIntegrationTest {

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
    private SimulationService simulationService;

    @Autowired
    private ParentDashboardService parentDashboardService;

    @Test
    void newChildShouldHaveEmptyDashboardActivity() {

        ParentResponse parent =
                createParent(
                        "dashboard_parent_one",
                        "dashboard.one@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "dashboard_child_one"
                );

        var dashboard =
                parentDashboardService
                        .getDashboard(
                                parent.userId(),
                                child.id()
                        );

        assertThat(dashboard.childId())
                .isEqualTo(
                        child.id()
                );

        assertThat(dashboard.xp())
                .isZero();

        assertThat(dashboard.level())
                .isEqualTo(1);

        assertThat(dashboard.overallScore())
                .isZero();

        assertThat(dashboard.completedChallenges())
                .isZero();

        assertThat(dashboard.completedSimulations())
                .isZero();

        assertThat(dashboard.completedActivities())
                .isZero();

        assertThat(dashboard.earnedAchievements())
                .isZero();

        assertThat(dashboard.skillScores())
                .hasSize(5);

        assertThat(dashboard.areasForImprovement())
                .isEmpty();
    }

    @Test
    void dashboardShouldAggregateChildActivity() {

        ParentResponse parent =
                createParent(
                        "dashboard_parent_two",
                        "dashboard.two@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "dashboard_child_two"
                );

        challengeService.submitAnswerForChild(
                child.userId(),
                "SAVING_01",
                new ChallengeAnswerRequest(
                        "SAVE_FIRST"
                )
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

        var dashboard =
                parentDashboardService
                        .getDashboard(
                                parent.userId(),
                                child.id()
                        );

        assertThat(dashboard.xp())
                .isEqualTo(20);

        assertThat(dashboard.completedChallenges())
                .isEqualTo(1);

        assertThat(dashboard.completedSimulations())
                .isEqualTo(1);

        assertThat(dashboard.completedActivities())
                .isEqualTo(2);

        assertThat(dashboard.streak().currentStreak())
                .isEqualTo(1);

        assertThat(dashboard.streak().activeToday())
                .isTrue();

        assertThat(dashboard.overallScore())
                .isEqualTo(100);

        assertThat(dashboard.earnedAchievements())
                .isGreaterThanOrEqualTo(2);

        assertThat(dashboard.areasForImprovement())
                .isEmpty();
    }

    @Test
    void parentShouldNotAccessAnotherParentsChildDashboard() {

        ParentResponse owner =
                createParent(
                        "dashboard_owner",
                        "dashboard.owner@example.com"
                );

        ChildResponse child =
                createChild(
                        owner,
                        "dashboard_owner_child"
                );

        ParentResponse otherParent =
                createParent(
                        "dashboard_other_parent",
                        "dashboard.other@example.com"
                );

        assertThatThrownBy(() ->
                parentDashboardService
                        .getDashboard(
                                otherParent.userId(),
                                child.id()
                        )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Child not found for authenticated parent"
                );
    }

    private ParentResponse createParent(
            String username,
            String email
    ) {

        return parentService.createParent(
                new CreateParentRequest(
                        username,
                        "Password123!",
                        "Dashboard Parent",
                        email
                )
        );
    }

    private ChildResponse createChild(
            ParentResponse parent,
            String username
    ) {

        return childService.createChildForParent(
                parent.userId(),
                new CreateChildRequest(
                        username,
                        "Password123!",
                        "Dashboard Child",
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