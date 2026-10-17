package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.learning.LearningTopicRepository;
import com.kidsfinance.parent.ParentService;
import com.kidsfinance.parent.dto.request.CreateParentRequest;
import com.kidsfinance.parent.dto.response.ParentResponse;
import com.kidsfinance.simulation.dto.request.SimulationDecisionRequest;
import com.kidsfinance.simulation.dto.response.SimulationDecisionResponse;
import com.kidsfinance.wallet.Wallet;
import com.kidsfinance.wallet.WalletRepository;
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
class SimulationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private LearningTopicRepository learningTopicRepository;

    @Autowired
    private SimulationScenarioRepository simulationScenarioRepository;

    @Autowired
    private SimulationOptionRepository simulationOptionRepository;

    @Autowired
    private SimulationDecisionRepository simulationDecisionRepository;

    @Autowired
    private SimulationRunRepository simulationRunRepository;

    @Autowired
    private ParentService parentService;

    @Autowired
    private ChildService childService;

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ChildProfileRepository childProfileRepository;

    @Test
    void shouldLoadSeededSimulationCatalog() {

        assertThat(learningTopicRepository.count())
                .isEqualTo(4);

        assertThat(simulationScenarioRepository.count())
                .isEqualTo(2);

        assertThat(simulationOptionRepository.count())
                .isEqualTo(4);
    }

    @Test
    void shouldProcessSimulationDecisionAndUpdateWalletAndXp() {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "integration_parent",
                                "Password123!",
                                "Integration Parent",
                                "integration.parent@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "integration_child",
                                "Password123!",
                                "Integration Child",
                                (short) 12
                        )
                );

        Wallet walletBefore =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(walletBefore.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        SimulationDecisionResponse decision =
                simulationService.makeDecision(
                        parent.userId(),
                        child.id(),
                        "SAVE_OR_SPEND_01",
                        new SimulationDecisionRequest(
                                "BUY_HEADPHONES"
                        )
                );

        assertThat(decision.id())
                .isNotNull();

        assertThat(decision.runId())
                .isNotNull();

        assertThat(decision.scenarioCode())
                .isEqualTo(
                        "SAVE_OR_SPEND_01"
                );

        assertThat(decision.optionCode())
                .isEqualTo(
                        "BUY_HEADPHONES"
                );

        assertThat(decision.amountSpent())
                .isEqualByComparingTo(
                        new BigDecimal("150.00")
                );

        assertThat(decision.xpEarned())
                .isEqualTo(10);

        assertThat(decision.walletTransactionId())
                .isNotNull();

        SimulationRun run =
                simulationRunRepository
                        .findById(decision.runId())
                        .orElseThrow();

        assertThat(run.getStatus())
                .isEqualTo(
                        SimulationRunStatus.COMPLETED
                );

        assertThat(run.getCompletedAt())
                .isNotNull();

        var runHistory =
                simulationService.getRunsForParent(
                        parent.userId(),
                        child.id()
                );

        assertThat(runHistory)
                .hasSize(1);

        assertThat(runHistory.getFirst().id())
                .isEqualTo(decision.runId());

        assertThat(runHistory.getFirst().scenarioCode())
                .isEqualTo("SAVE_OR_SPEND_01");

        assertThat(runHistory.getFirst().status())
                .isEqualTo(
                        SimulationRunStatus.COMPLETED
                );

        Wallet walletAfter =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(walletAfter.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("850.00")
                );

        ChildProfile childAfter =
                childProfileRepository
                        .findById(child.id())
                        .orElseThrow();

        assertThat(childAfter.getXp())
                .isEqualTo(10);

        assertThat(childAfter.getLevel())
                .isEqualTo(1);

        assertThat(
                simulationDecisionRepository
                        .existsByChild_IdAndScenarioCode(
                                child.id(),
                                "SAVE_OR_SPEND_01"
                        )
        ).isTrue();
    }

    @Test
    void shouldRejectDecisionWhenChildDoesNotBelongToParent() {

        ParentResponse parentA =
                parentService.createParent(
                        new CreateParentRequest(
                                "ownership_parent_a",
                                "Password123!",
                                "Ownership Parent A",
                                "ownership.parent.a@example.com"
                        )
                );

        ParentResponse parentB =
                parentService.createParent(
                        new CreateParentRequest(
                                "ownership_parent_b",
                                "Password123!",
                                "Ownership Parent B",
                                "ownership.parent.b@example.com"
                        )
                );

        ChildResponse childOfParentB =
                childService.createChildForParent(
                        parentB.userId(),
                        new CreateChildRequest(
                                "ownership_child_b",
                                "Password123!",
                                "Ownership Child B",
                                (short) 12
                        )
                );

        Wallet walletBefore =
                walletRepository
                        .findByChild_Id(
                                childOfParentB.id()
                        )
                        .orElseThrow();

        assertThat(walletBefore.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        assertThatThrownBy(() ->
                simulationService.makeDecision(
                        parentA.userId(),
                        childOfParentB.id(),
                        "SAVE_OR_SPEND_01",
                        new SimulationDecisionRequest(
                                "BUY_HEADPHONES"
                        )
                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Child not found for authenticated parent"
                );

        Wallet walletAfter =
                walletRepository
                        .findByChild_Id(
                                childOfParentB.id()
                        )
                        .orElseThrow();

        assertThat(walletAfter.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        assertThat(
                simulationDecisionRepository
                        .existsByChild_IdAndScenarioCode(
                                childOfParentB.id(),
                                "SAVE_OR_SPEND_01"
                        )
        ).isFalse();

        ChildProfile childAfter =
                childProfileRepository
                        .findById(
                                childOfParentB.id()
                        )
                        .orElseThrow();

        assertThat(childAfter.getXp())
                .isZero();
    }

    @Test
    void shouldRejectUnknownScenario() {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "invalid_scenario_parent",
                                "Password123!",
                                "Invalid Scenario Parent",
                                "invalid.scenario@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "invalid_scenario_child",
                                "Password123!",
                                "Invalid Scenario Child",
                                (short) 12
                        )
                );

        assertThatThrownBy(() ->
                simulationService.makeDecision(
                        parent.userId(),
                        child.id(),
                        "UNKNOWN_SCENARIO",
                        new SimulationDecisionRequest(
                                "BUY_HEADPHONES"
                        )
                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Simulation scenario not found"
                );

        Wallet wallet =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(wallet.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        ChildProfile childAfter =
                childProfileRepository
                        .findById(child.id())
                        .orElseThrow();

        assertThat(childAfter.getXp())
                .isZero();
    }

    @Test
    void shouldRejectOptionThatDoesNotBelongToScenario() {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "invalid_option_parent",
                                "Password123!",
                                "Invalid Option Parent",
                                "invalid.option@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "invalid_option_child",
                                "Password123!",
                                "Invalid Option Child",
                                (short) 12
                        )
                );

        assertThatThrownBy(() ->
                simulationService.makeDecision(
                        parent.userId(),
                        child.id(),
                        "SAVE_OR_SPEND_01",
                        new SimulationDecisionRequest(
                                "BUY_SCHOOL_BAG"
                        )
                )
        )
                .isInstanceOf(
                        ResourceNotFoundException.class
                )
                .hasMessage(
                        "Simulation option not found"
                );

        Wallet wallet =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(wallet.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        ChildProfile childAfter =
                childProfileRepository
                        .findById(child.id())
                        .orElseThrow();

        assertThat(childAfter.getXp())
                .isZero();

        assertThat(
                simulationDecisionRepository
                        .existsByChild_IdAndScenarioCode(
                                child.id(),
                                "SAVE_OR_SPEND_01"
                        )
        ).isFalse();
    }

    @Test
    void shouldAllowChildToReplaySameScenario() {

        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "replay_parent",
                                "Password123!",
                                "Replay Parent",
                                "replay.parent@example.com"
                        )
                );

        ChildResponse child =
                childService.createChildForParent(
                        parent.userId(),
                        new CreateChildRequest(
                                "replay_child",
                                "Password123!",
                                "Replay Child",
                                (short) 12
                        )
                );

        SimulationDecisionResponse firstDecision =
                simulationService.makeDecision(
                        parent.userId(),
                        child.id(),
                        "SAVE_OR_SPEND_01",
                        new SimulationDecisionRequest(
                                "BUY_HEADPHONES"
                        )
                );

        SimulationDecisionResponse secondDecision =
                simulationService.makeDecision(
                        parent.userId(),
                        child.id(),
                        "SAVE_OR_SPEND_01",
                        new SimulationDecisionRequest(
                                "KEEP_SAVINGS"
                        )
                );

        assertThat(firstDecision.runId())
                .isNotNull();

        assertThat(secondDecision.runId())
                .isNotNull();

        assertThat(secondDecision.runId())
                .isNotEqualTo(
                        firstDecision.runId()
                );

        assertThat(
                simulationDecisionRepository
                        .findAllByChild_IdOrderByCreatedAtDescIdDesc(
                                child.id()
                        )
        ).hasSize(2);

        var runs =
                simulationRunRepository
                        .findAllByChild_IdOrderByStartedAtDescIdDesc(
                                child.id()
                        );

        assertThat(runs)
                .hasSize(2);

        assertThat(runs)
                .allMatch(
                        run ->
                                run.getStatus()
                                        == SimulationRunStatus.COMPLETED
                );

        assertThat(runs)
                .allMatch(
                        run ->
                                run.getCompletedAt() != null
                );

        var runHistory =
                simulationService.getRunsForParent(
                        parent.userId(),
                        child.id()
                );

        assertThat(runHistory)
                .hasSize(2);

        assertThat(runHistory)
                .allMatch(
                        run ->
                                run.status()
                                        == SimulationRunStatus.COMPLETED
                );

        Wallet wallet =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(wallet.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("850.00")
                );

        ChildProfile childAfter =
                childProfileRepository
                        .findById(child.id())
                        .orElseThrow();

        assertThat(childAfter.getXp())
                .isEqualTo(20);

        assertThat(childAfter.getLevel())
                .isEqualTo(1);
    }
}