package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.child.ChildService;
import com.kidsfinance.child.dto.request.CreateChildRequest;
import com.kidsfinance.child.dto.response.ChildResponse;
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

import com.kidsfinance.common.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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

        // 1. Create parent
        ParentResponse parent =
                parentService.createParent(
                        new CreateParentRequest(
                                "integration_parent",
                                "Password123!",
                                "Integration Parent",
                                "integration.parent@example.com"
                        )
                );

        // 2. Create child
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

        // 3. Initial wallet = 1000 AZN
        Wallet walletBefore =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(walletBefore.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        // 4. Execute simulation decision
        SimulationDecisionResponse decision =
                simulationService.makeDecision(
                        parent.userId(),
                        child.id(),
                        "SAVE_OR_SPEND_01",
                        new SimulationDecisionRequest(
                                "BUY_HEADPHONES"
                        )
                );

        // 5. Verify decision data
        assertThat(decision.scenarioCode())
                .isEqualTo("SAVE_OR_SPEND_01");

        assertThat(decision.optionCode())
                .isEqualTo("BUY_HEADPHONES");

        assertThat(decision.amountSpent())
                .isEqualByComparingTo(
                        new BigDecimal("150.00")
                );

        assertThat(decision.xpEarned())
                .isEqualTo(10);

        assertThat(decision.walletTransactionId())
                .isNotNull();

        // 6. Wallet = 1000 - 150 = 850
        Wallet walletAfter =
                walletRepository
                        .findByChild_Id(child.id())
                        .orElseThrow();

        assertThat(walletAfter.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("850.00")
                );

        // 7. Child receives XP
        ChildProfile childAfter =
                childProfileRepository
                        .findById(child.id())
                        .orElseThrow();

        assertThat(childAfter.getXp())
                .isEqualTo(10);

        assertThat(childAfter.getLevel())
                .isEqualTo(1);

        // 8. Decision really exists
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

        // Parent A
        ParentResponse parentA =
                parentService.createParent(
                        new CreateParentRequest(
                                "ownership_parent_a",
                                "Password123!",
                                "Ownership Parent A",
                                "ownership.parent.a@example.com"
                        )
                );

        // Parent B
        ParentResponse parentB =
                parentService.createParent(
                        new CreateParentRequest(
                                "ownership_parent_b",
                                "Password123!",
                                "Ownership Parent B",
                                "ownership.parent.b@example.com"
                        )
                );

        // Child belongs only to Parent B
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

        // Wallet before attack attempt
        Wallet walletBefore =
                walletRepository
                        .findByChild_Id(childOfParentB.id())
                        .orElseThrow();

        assertThat(walletBefore.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        // Parent A must NOT be able to make a decision
        // for Parent B's child.
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
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Child not found for authenticated parent"
                );

        // Wallet must remain unchanged
        Wallet walletAfter =
                walletRepository
                        .findByChild_Id(childOfParentB.id())
                        .orElseThrow();

        assertThat(walletAfter.getBalance())
                .isEqualByComparingTo(
                        new BigDecimal("1000.00")
                );

        // No decision should be persisted
        assertThat(
                simulationDecisionRepository
                        .existsByChild_IdAndScenarioCode(
                                childOfParentB.id(),
                                "SAVE_OR_SPEND_01"
                        )
        ).isFalse();

        // No XP should be awarded
        ChildProfile childAfter =
                childProfileRepository
                        .findById(childOfParentB.id())
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
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Simulation scenario not found");

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

        /*
         * BUY_SCHOOL_BAG exists,
         * but belongs to PLAN_PURCHASE_02,
         * not SAVE_OR_SPEND_01.
         */
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
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Simulation option not found");

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

        simulationService.makeDecision(
                parent.userId(),
                child.id(),
                "SAVE_OR_SPEND_01",
                new SimulationDecisionRequest(
                        "BUY_HEADPHONES"
                )
        );

        simulationService.makeDecision(
                parent.userId(),
                child.id(),
                "SAVE_OR_SPEND_01",
                new SimulationDecisionRequest(
                        "KEEP_SAVINGS"
                )
        );

        assertThat(
                simulationDecisionRepository
                        .findAllByChild_IdOrderByCreatedAtDescIdDesc(
                                child.id()
                        )
        ).hasSize(2);

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
    }
}