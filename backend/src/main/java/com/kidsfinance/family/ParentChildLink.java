package com.kidsfinance.family;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.parent.ParentProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "parent_child_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_parent_child_link",
                        columnNames = {
                                "parent_profile_id",
                                "child_profile_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentChildLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "parent_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_parent_child_links_parent"
            )
    )
    private ParentProfile parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "child_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_parent_child_links_child"
            )
    )
    private ChildProfile child;

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