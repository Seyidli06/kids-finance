package com.kidsfinance.challenge;

import com.kidsfinance.learning.LearningTopic;
import jakarta.persistence.*;
import lombok.*;
import com.kidsfinance.scoring.SkillType;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "challenges",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_challenges_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "code",
            nullable = false,
            length = 50
    )
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "learning_topic_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_challenges_learning_topic"
            )
    )
    private LearningTopic learningTopic;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "skill_type",
            nullable = false,
            length = 30
    )
    private SkillType skillType;

    @Column(
            name = "title",
            nullable = false,
            length = 150
    )
    private String title;

    @Column(
            name = "prompt",
            nullable = false,
            length = 1000
    )
    private String prompt;

    @Column(
            name = "explanation",
            nullable = false,
            length = 1000
    )
    private String explanation;

    @Column(
            name = "xp_reward",
            nullable = false
    )
    private Integer xpReward;

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