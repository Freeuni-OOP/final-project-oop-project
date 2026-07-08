package com.quizwebsite.model;

import com.quizwebsite.util.TextMatcher;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Multi-answer extension: the user supplies several text answers.
 *
 * Ordered mode checks answer slots position-by-position. Unordered mode awards
 * each distinct legal answer once, no matter which input slot contains it.
 */
@Entity
@DiscriminatorValue(MultiAnswerQuestion.TYPE)
public class MultiAnswerQuestion extends Question {

    public static final String TYPE = "MULTI_ANSWER";

    @Override
    @Transient
    public String getType() { return TYPE; }

    @Override
    @Transient
    public int getMaxScore() {
        return Math.max(1, getAnswerSlots() > 0 ? getAnswerSlots() : getAnswers().size());
    }

    @Override
    public int grade(String[] userResponses) {
        if (userResponses == null || userResponses.length == 0) return 0;
        return isOrdered() ? gradeOrdered(userResponses) : gradeUnordered(userResponses);
    }

    private int gradeOrdered(String[] userResponses) {
        int correct = 0;
        int slots = Math.min(Math.min(userResponses.length, getAnswers().size()), getMaxScore());
        for (int i = 0; i < slots; i++) {
            if (TextMatcher.matchesAny(userResponses[i], List.of(getAnswers().get(i).getText()))) {
                correct++;
            }
        }
        return correct;
    }

    private int gradeUnordered(String[] userResponses) {
        Set<String> legal = getAnswers().stream()
                .map(Answer::getText)
                .map(TextMatcher::normalize)
                .collect(Collectors.toSet());
        Set<String> used = new HashSet<>();
        int correct = 0;
        for (String response : userResponses) {
            String normalized = TextMatcher.normalize(response);
            if (legal.contains(normalized) && used.add(normalized)) {
                correct++;
            }
            if (correct == getMaxScore()) break;
        }
        return correct;
    }
}
