package com.quizwebsite.model.activity;

import com.quizwebsite.util.TimeFormat;

/** Aggregate performance numbers shown on the quiz summary page. */
public class QuizStatistics {

    private final long attemptCount;
    private final double averageScore;
    private final double averagePercent;
    private final double averageTimeSeconds;
    private final int bestScore;

    public QuizStatistics(long attemptCount,
                          double averageScore,
                          double averagePercent,
                          double averageTimeSeconds,
                          int bestScore) {
        this.attemptCount = attemptCount;
        this.averageScore = averageScore;
        this.averagePercent = averagePercent;
        this.averageTimeSeconds = averageTimeSeconds;
        this.bestScore = bestScore;
    }

    public long getAttemptCount() { return attemptCount; }
    public double getAverageScore() { return averageScore; }
    public double getAveragePercent() { return averagePercent; }
    public double getAverageTimeSeconds() { return averageTimeSeconds; }
    public int getBestScore() { return bestScore; }

    public boolean isEmpty() { return attemptCount == 0; }

    public String getAverageTimeFormatted() {
        return TimeFormat.mmss((int) Math.round(averageTimeSeconds));
    }
}
