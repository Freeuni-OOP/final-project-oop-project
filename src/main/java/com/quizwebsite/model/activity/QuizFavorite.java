package com.quizwebsite.model.activity;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "quiz_favorites",
        uniqueConstraints = @UniqueConstraint(name = "uniq_quiz_favorite", columnNames = {"user_id", "quiz_id"}))
@Getter
@Setter
@NoArgsConstructor
public class QuizFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    private LocalDateTime createdAt = LocalDateTime.now();

    public QuizFavorite(User user, Quiz quiz) {
        this.user = user;
        this.quiz = quiz;
    }
}
