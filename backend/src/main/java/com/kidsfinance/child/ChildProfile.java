package com.kidsfinance.child;

import com.kidsfinance.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "child_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_child_profiles_user_id",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_child_profiles_user"
            )
    )
    private User user;

    @Column(
            name = "display_name",
            nullable = false,
            length = 50
    )
    private String displayName;

    @Column(
            name = "age",
            nullable = false
    )
    private Short age;

    @Builder.Default
    @Column(
            name = "xp",
            nullable = false
    )
    private Integer xp = 0;

    @Builder.Default
    @Column(
            name = "level",
            nullable = false
    )
    private Integer level = 1;

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