package com.quizwebsite.model.achievement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One earned achievement. Maps to {@code achievements}, with a UNIQUE (user_id, kind)
 * constraint so the same badge is never awarded twice.
 */
@Entity
@Table(name = "achievements",
        uniqueConstraints = @UniqueConstraint(name = "uniq_user_achievement", columnNames = {"user_id", "kind"}))
@Getter
@Setter
@NoArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 40)
    private String kind;

    @Column(name = "awarded_at", nullable = false, updatable = false)
    private LocalDateTime awardedAt = LocalDateTime.now();

    public Achievement(Integer userId, String kind) {
        this.userId = userId;
        this.kind = kind;
    }

    /** Convenience for views: lookup of the human-readable label / tooltip. */
    @Transient
    public AchievementKind getKindEnum() { return AchievementKind.fromKey(kind); }
}
