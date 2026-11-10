package com.kidsfinance.gamification;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "child_achievements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_child_achievements_child_achievement",
                        columnNames = {
                                "child_profile_id",
                                "achievement_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_child_achievements_child"
            )
    )
    private ChildProfile child;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "achievement_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_child_achievements_achievement"
            )
    )
    private Achievement achievement;

    @Column(
            name = "earned_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime earnedAt;

    @PrePersist
    void onCreate() {
        if (earnedAt == null) {
            earnedAt = OffsetDateTime.now();
        }
    }
}