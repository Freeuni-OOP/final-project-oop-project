package com.quizwebsite.model.achievement;

import java.time.LocalDateTime;

/** View model for recent achievement activity by friends. */
public class AchievementActivity {

    private final Integer userId;
    private final String username;
    private final String kind;
    private final LocalDateTime awardedAt;

    public AchievementActivity(Integer userId, String username, String kind, LocalDateTime awardedAt) {
        this.userId = userId;
        this.username = username;
        this.kind = kind;
        this.awardedAt = awardedAt;
    }

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getKind() { return kind; }
    public LocalDateTime getAwardedAt() { return awardedAt; }
    public AchievementKind getKindEnum() { return AchievementKind.fromKey(kind); }
}
