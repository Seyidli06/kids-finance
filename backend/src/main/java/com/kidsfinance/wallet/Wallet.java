package com.kidsfinance.wallet;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wallets_child_profile_id",
                        columnNames = "child_profile_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_wallets_child"
            )
    )
    private ChildProfile child;

    @Column(
            name = "balance",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    private WalletCurrency currency;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        if (balance == null) {
            balance = new BigDecimal("1000.00");
        }

        if (currency == null) {
            currency = WalletCurrency.AZN;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}