package com.quizwebsite.model.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One row from the {@code answers} table.
 *
 * For text-input questions each Answer is an acceptable user response ({@code correct}
 * is always true). For multi-answer questions, {@code position} can define the
 * expected order. For choice-based questions each Answer is a displayed choice and
 * {@code correct} flags whether picking it earns a point.
 */
@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, length = 500)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean correct = true;

    @Column(name = "position", nullable = false)
    private int position;

    public Answer(String text, boolean correct, int position) {
        this.text = text;
        this.correct = correct;
        this.position = position;
    }
}
