package com.kidsfinance.scoring;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "child_skill_scores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_child_skill_scores_child_skill",
                        columnNames = {
                                "child_profile_id",
                                "skill_type"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildSkillScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_child_skill_scores_child"
            )
    )
    private ChildProfile child;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "skill_type",
            nullable = false,
            length = 30
    )
    private SkillType skillType;

    @Builder.Default
    @Column(
            name = "score",
            nullable = false
    )
    private Integer score = 0;

    @Builder.Default
    @Column(
            name = "activity_count",
            nullable = false
    )
    private Integer activityCount = 0;

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