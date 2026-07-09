package com.quizwebsite.model.question;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.List;

/**
 * Radio-button question: the user picks exactly one of the displayed choices.
 *
 * The HTTP form submits the chosen answer's database id as the response string.
 * (We persist answer ids as stable handles, so they survive shuffles.)
 */
@Entity
@DiscriminatorValue(MultipleChoiceQuestion.TYPE)
public class MultipleChoiceQuestion extends Question {

    public static final String TYPE = "MULTIPLE_CHOICE";

    @Override
    @Transient
    public String getType() { return TYPE; }

    @Override
    @Transient
    public int getMaxScore() { return 1; }

    @Override
    public int grade(String[] userResponses) {
        if (userResponses == null || userResponses.length == 0) return 0;
        String picked = userResponses[0];
        if (picked == null || picked.isEmpty()) return 0;

        int pickedId;
        try {
            pickedId = Integer.parseInt(picked.trim());
        } catch (NumberFormatException e) {
            return 0;
        }

        for (Answer a : getAnswers()) {
            if (a.getId() != null && a.getId() == pickedId) return a.isCorrect() ? 1 : 0;
        }
        return 0;
    }

    /** Convenience for renderers: the choices in their stored display order. */
    @Transient
    public List<Answer> getChoices() { return getAnswers(); }
}
