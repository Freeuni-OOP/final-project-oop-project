package com.quizwebsite.model;

import java.time.LocalDateTime;

/** View model for history rows that may need privacy-aware user display. */
public class HistoryActivity {

    private final Integer userId;
    private final String username;
    private final boolean identityVisible;
    private final Integer quizId;
    private final String quizName;
    private final int score;
    private final int maxScore;
    private final int timeTakenSeconds;
    private final String timeTakenFormatted;
    private final LocalDateTime takenAt;

    public HistoryActivity(QuizHistoryEntry entry, boolean identityVisible) {
        this.userId = entry.getUserId();
        this.username = identityVisible ? entry.getUsername() : "anonymous";
        this.identityVisible = identityVisible;
        this.quizId = entry.getQuizId();
        this.quizName = entry.getQuizName();
        this.score = entry.getScore();
        this.maxScore = entry.getMaxScore();
        this.timeTakenSeconds = entry.getTimeTakenSeconds();
        this.timeTakenFormatted = entry.getTimeTakenFormatted();
        this.takenAt = entry.getTakenAt();
    }

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isIdentityVisible() { return identityVisible; }
    public Integer getQuizId() { return quizId; }
    public String getQuizName() { return quizName; }
    public int getScore() { return score; }
    public int getMaxScore() { return maxScore; }
    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public String getTimeTakenFormatted() { return timeTakenFormatted; }
    public LocalDateTime getTakenAt() { return takenAt; }
}
