package com.kidsfinance.reporting;

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
class ParentReportIntegrationTest {

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
    private ParentReportService parentReportService;

    @Test
    void shouldGenerateWeeklyReportFromChildActivity() {

        ParentResponse parent =
                createParent(
                        "report_parent_one",
                        "report.one@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "report_child_one"
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

        simulationService.runForChild(
                child.userId(),
                balancedSimulation()
        );

        var report =
                parentReportService.getReport(
                        parent.userId(),
                        child.id(),
                        ReportPeriod.WEEKLY
                );

        assertThat(report.period())
                .isEqualTo(
                        ReportPeriod.WEEKLY
                );

        assertThat(report.challengeAttempts())
                .isEqualTo(2);

        assertThat(report.correctChallengeAnswers())
                .isEqualTo(1);

        assertThat(report.simulationRuns())
                .isEqualTo(1);

        assertThat(report.totalActivities())
                .isEqualTo(3);

        assertThat(report.xpEarned())
                .isEqualTo(20);

        assertThat(report.activeDays())
                .isEqualTo(1);

        assertThat(report.dailyActivity())
                .isNotEmpty();

        assertThat(
                report.dailyActivity()
                        .stream()
                        .mapToLong(day ->
                                day.totalActivities()
                        )
                        .sum()
        ).isEqualTo(3);
    }

    @Test
    void shouldGenerateMonthlyReport() {

        ParentResponse parent =
                createParent(
                        "report_parent_two",
                        "report.two@example.com"
                );

        ChildResponse child =
                createChild(
                        parent,
                        "report_child_two"
                );

        simulationService.runForChild(
                child.userId(),
                balancedSimulation()
        );

        var report =
                parentReportService.getReport(
                        parent.userId(),
                        child.id(),
                        ReportPeriod.MONTHLY
                );

        assertThat(report.period())
                .isEqualTo(
                        ReportPeriod.MONTHLY
                );

        assertThat(report.simulationRuns())
                .isEqualTo(1);

        assertThat(report.totalActivities())
                .isEqualTo(1);

        assertThat(report.fromDate().getDayOfMonth())
                .isEqualTo(1);
    }

    @Test
    void parentShouldNotReadAnotherParentsReport() {

        ParentResponse owner =
                createParent(
                        "report_owner",
                        "report.owner@example.com"
                );

        ChildResponse child =
                createChild(
                        owner,
                        "report_owner_child"
                );

        ParentResponse otherParent =
                createParent(
                        "report_other",
                        "report.other@example.com"
                );

        assertThatThrownBy(() ->
                parentReportService.getReport(
                        otherParent.userId(),
                        child.id(),
                        ReportPeriod.WEEKLY
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
                        "Report Parent",
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
                        "Report Child",
                        (short) 12
                )
        );
    }

    private SimulationRequest balancedSimulation() {

        return new SimulationRequest(
                amount("100"),
                amount("200"),
                amount("200"),
                amount("100"),
                amount("150"),
                amount("100"),
                amount("150")
        );
    }

    private BigDecimal amount(
            String value
    ) {
        return new BigDecimal(value);
    }
}