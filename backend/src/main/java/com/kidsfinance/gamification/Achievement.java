package com.kidsfinance.gamification;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "achievements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_achievements_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "code",
            nullable = false,
            length = 50
    )
    private String code;

    @Column(
            name = "title",
            nullable = false,
            length = 100
    )
    private String title;

    @Column(
            name = "description",
            nullable = false,
            length = 500
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "condition_type",
            nullable = false,
            length = 40
    )
    private AchievementConditionType conditionType;

    @Column(
            name = "threshold",
            nullable = false
    )
    private Integer threshold;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder;

    @Builder.Default
    @Column(
            name = "active",
            nullable = false
    )
    private Boolean active = true;

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