package com.quizwebsite.model.question;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.HashSet;
import java.util.Set;

/**
 * Multiple-choice extension where more than one displayed choice can be correct.
 *
 * Selected correct choices add points; selected incorrect choices subtract points,
 * and the final score never drops below zero.
 */
@Entity
@DiscriminatorValue(MultiSelectQuestion.TYPE)
public class MultiSelectQuestion extends Question {

    public static final String TYPE = "MULTI_SELECT";

    @Override
    @Transient
    public String getType() { return TYPE; }

    @Override
    @Transient
    public int getMaxScore() {
        int correct = 0;
        for (Answer answer : getAnswers()) {
            if (answer.isCorrect()) correct++;
        }
        return Math.max(1, correct);
    }

    @Override
    public int grade(String[] userResponses) {
        if (userResponses == null || userResponses.length == 0) return 0;
        Set<Integer> selectedIds = selectedAnswerIds(userResponses);
        int correctSelections = 0;
        int incorrectSelections = 0;
        for (Integer selectedId : selectedIds) {
            Answer selected = answerById(selectedId);
            if (selected != null && selected.isCorrect()) {
                correctSelections++;
            } else {
                incorrectSelections++;
            }
        }
        return Math.max(0, correctSelections - incorrectSelections);
    }

    private Set<Integer> selectedAnswerIds(String[] userResponses) {
        Set<Integer> ids = new HashSet<>();
        for (String response : userResponses) {
            try {
                ids.add(Integer.parseInt(response.trim()));
            } catch (RuntimeException ignored) {
                // Ignore malformed form values; normal submissions use answer ids.
            }
        }
        return ids;
    }

    private Answer answerById(int id) {
        for (Answer answer : getAnswers()) {
            if (answer.getId() != null && answer.getId() == id) return answer;
        }
        return null;
    }
}
