package com.quizwebsite.model;

import com.quizwebsite.util.TextMatcher;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard text question. The user types a free-text answer; the quiz creator
 * stored one or more acceptable answers. A match against any of them earns the point.
 */
@Entity
@DiscriminatorValue(QuestionResponseQuestion.TYPE)
public class QuestionResponseQuestion extends Question {

    public static final String TYPE = "QUESTION_RESPONSE";

    @Override
    @Transient
    public String getType() { return TYPE; }

    @Override
    @Transient
    public int getMaxScore() { return 1; }

    @Override
    public int grade(String[] userResponses) {
        if (userResponses == null || userResponses.length == 0) return 0;
        return TextMatcher.matchesAny(userResponses[0], acceptableAnswerTexts()) ? 1 : 0;
    }

    protected List<String> acceptableAnswerTexts() {
        List<String> out = new ArrayList<>(getAnswers().size());
        for (Answer a : getAnswers()) {
            if (a.isCorrect()) out.add(a.getText());
        }
        return out;
    }
}
