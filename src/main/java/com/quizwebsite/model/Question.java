package com.quizwebsite.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every question type, mapped single-table with the {@code type}
 * column as the JPA discriminator.
 *
 * Adding a new question type means: subclass this (or a subclass), give it a
 * {@code @DiscriminatorValue}, and implement {@link #getType()}, {@link #getMaxScore()},
 * and {@link #grade(String[])}. Hibernate instantiates the right subclass on read, so
 * no factory or switch statement is needed — existing code does not change.
 *
 * The user's submitted answer is passed as a {@code String[]} so that multi-input
 * question types can share the same API as single-input ones (which pass a 1-element array).
 */
@Entity
@Table(name = "questions")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", length = 40)
@Getter
@Setter
public abstract class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "ordered", nullable = false)
    private boolean ordered;

    @Column(name = "answer_slots", nullable = false)
    private int answerSlots;

    @Column(name = "position", nullable = false)
    private int position;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Answer> answers = new ArrayList<>();

    /** Stable string identifier; matches the discriminator value in {@code questions.type}. */
    @Transient
    public abstract String getType();

    /** Maximum points this question can earn (1 for the required types). */
    @Transient
    public abstract int getMaxScore();

    /** Returns the score the user earned for their response. Always in [0, getMaxScore()]. */
    public abstract int grade(String[] userResponses);

    /** Adds an answer and wires the back-reference. */
    public void addAnswer(Answer a) {
        a.setQuestion(this);
        answers.add(a);
    }

    /** Convenience for views: the quiz id without forcing a full load through getQuiz(). */
    @Transient
    public Integer getQuizId() {
        return quiz == null ? null : quiz.getId();
    }
}
