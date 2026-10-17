package com.kidsfinance.simulation;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "simulation_decisions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SimulationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_simulation_decisions_child"
            )
    )
    private ChildProfile child;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "run_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_simulation_decisions_run"
            )
    )
    private SimulationRun run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "option_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_simulation_decisions_option"
            )
    )
    private SimulationOption option;


    @Column(
            name = "scenario_code",
            nullable = false,
            length = 50
    )
    private String scenarioCode;

    @Column(
            name = "option_code",
            nullable = false,
            length = 50
    )
    private String optionCode;

    @Column(
            name = "amount_spent",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amountSpent;

    @Column(
            name = "wallet_transaction_id",
            unique = true
    )
    private Long walletTransactionId;

    @Column(
            name = "xp_earned",
            nullable = false
    )
    private Integer xpEarned;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }
}