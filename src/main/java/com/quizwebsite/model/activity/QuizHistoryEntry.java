package com.quizwebsite.model.activity;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.util.TimeFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * One completed (non-practice) attempt. Maps to {@code quiz_history}.
 *
 * The {@code user} / {@code quiz} associations are lazy; views read the joined
 * username / quiz name through the transient convenience getters (open-in-view
 * keeps the session open during rendering).
 */
@Entity
@Table(name = "quiz_history", indexes = {
        @Index(name = "idx_history_quiz_score", columnList = "quiz_id, score, time_taken_seconds"),
        @Index(name = "idx_history_user", columnList = "user_id, taken_at")
})
@Getter
@Setter
@NoArgsConstructor
public class QuizHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private int score;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(name = "time_taken_seconds", nullable = false)
    private int timeTakenSeconds;

    @Column(name = "taken_at", nullable = false, updatable = false)
    private LocalDateTime takenAt = LocalDateTime.now();

    public QuizHistoryEntry(User user, Quiz quiz, int score, int maxScore, int timeTakenSeconds) {
        this.user = user;
        this.quiz = quiz;
        this.score = score;
        this.maxScore = maxScore;
        this.timeTakenSeconds = timeTakenSeconds;
    }

    // ---- convenience accessors used by the views ----

    @Transient
    public Integer getUserId() { return user == null ? null : user.getId(); }

    @Transient
    public String getUsername() { return user == null ? null : user.getUsername(); }

    @Transient
    public Integer getQuizId() { return quiz == null ? null : quiz.getId(); }

    @Transient
    public String getQuizName() { return quiz == null ? null : quiz.getName(); }

    /** Convenience for views: time as "Xm Ys". */
    @Transient
    public String getTimeTakenFormatted() {
        return TimeFormat.mmss(timeTakenSeconds);
    }
}
