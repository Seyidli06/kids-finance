package com.kidsfinance.challenge;

import com.kidsfinance.child.ChildProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "challenge_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_challenge_attempts_child"
            )
    )
    private ChildProfile child;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "challenge_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_challenge_attempts_challenge"
            )
    )
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "selected_option_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_challenge_attempts_option"
            )
    )
    private ChallengeOption selectedOption;

    @Column(
            name = "correct",
            nullable = false
    )
    private boolean correct;

    @Column(
            name = "xp_awarded",
            nullable = false
    )
    private Integer xpAwarded;

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