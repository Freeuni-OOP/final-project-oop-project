package com.quizwebsite.model.activity;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "quiz_reports")
@Getter
@Setter
@NoArgsConstructor
public class QuizReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public QuizReport(Quiz quiz, User reporter, String reason) {
        this.quiz = quiz;
        this.reporter = reporter;
        this.reason = reason;
    }

    public void resolve(ReportStatus status) {
        this.status = status == null ? ReportStatus.RESOLVED : status;
        this.resolvedAt = LocalDateTime.now();
    }

    @Transient
    public String getQuizName() { return quiz == null ? null : quiz.getName(); }

    @Transient
    public Integer getQuizId() { return quiz == null ? null : quiz.getId(); }

    @Transient
    public String getReporterUsername() { return reporter == null ? null : reporter.getUsername(); }
}
