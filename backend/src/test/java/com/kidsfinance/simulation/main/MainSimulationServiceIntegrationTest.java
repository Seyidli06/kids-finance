package com.kidsfinance.simulation.main;

import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import com.kidsfinance.simulation.main.dto.request.MainSimulationRequest;
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
class MainSimulationServiceIntegrationTest {

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
    private MainSimulationService mainSimulationService;

    @Test
    void shouldRunMainSimulationAndApplyEvent() {

        ChildResponse child =
                createChild(
                        "main_sim_parent",
                        "main_sim_child",
                        "main.sim@example.com"
                );

        MainSimulationRequest request =
                new MainSimulationRequest(
                        amount("100"),
                        amount("200"),
                        amount("200"),
                        amount("100"),
                        amount("150"),
                        amount("100"),
                        amount("150")
                );

        var result =
                mainSimulationService
                        .runForChild(
                                child.userId(),
                                request
                        );

        assertThat(result.startingAmount())
                .isEqualByComparingTo("1000.00");

        assertThat(
                result.portfolioValueBeforeEvent()
        ).isEqualByComparingTo("900.00");

        assertThat(result.eventType())
                .isNotNull();

        assertThat(result.endingAmount())
                .isGreaterThanOrEqualTo(
                        BigDecimal.ZERO
                );

        assertThat(result.xpEarned())
                .isEqualTo(10);

        verifyEventMath(
                result.eventType(),
                result.eventDelta()
        );

        var secondRun =
                mainSimulationService
                        .runForChild(
                                child.userId(),
                                request
                        );

        assertThat(secondRun.xpEarned())
                .isZero();

        var history =
                mainSimulationService
                        .getRunsForChild(
                                child.userId()
                        );

        assertThat(history)
                .hasSize(2);
    }

    @Test
    void shouldRejectAllocationThatDoesNotTotalOneThousand() {

        ChildResponse child =
                createChild(
                        "invalid_main_parent",
                        "invalid_main_child",
                        "invalid.main@example.com"
                );

        MainSimulationRequest request =
                new MainSimulationRequest(
                        amount("100"),
                        amount("100"),
                        amount("100"),
                        amount("100"),
                        amount("100"),
                        amount("100"),
                        amount("100")
                );

        assertThatThrownBy(() ->
                mainSimulationService
                        .runForChild(
                                child.userId(),
                                request
                        )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessage(
                        "Allocations must total exactly 1000.00 AZN"
                );
    }

    private void verifyEventMath(
            MainSimulationEventType event,
            BigDecimal delta
    ) {

        switch (event) {

            case STOCK_MARKET_DROP ->
                    assertThat(delta)
                            .isEqualByComparingTo(
                                    "-22.50"
                            );

            case UNEXPECTED_EXPENSE ->
                    assertThat(delta)
                            .isEqualByComparingTo(
                                    "-200.00"
                            );

            case DEPOSIT_INTEREST ->
                    assertThat(delta)
                            .isEqualByComparingTo(
                                    "12.00"
                            );

            case BUSINESS_PROFIT ->
                    assertThat(delta)
                            .isEqualByComparingTo(
                                    "30.00"
                            );
        }
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
                                "Main Simulation Parent",
                                email
                        )
                );

        return childService
                .createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                childUsername,
                                "Password123!",
                                "Main Simulation Child",
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