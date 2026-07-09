package com.quizwebsite.util;

import com.quizwebsite.model.question.Answer;
import com.quizwebsite.model.question.MultiSelectQuestion;
import com.quizwebsite.model.question.MultipleChoiceQuestion;
import com.quizwebsite.model.question.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns raw HTTP-form responses into human-readable text for the results page.
 *
 * For multiple-choice the form sends an answer id; we look up the choice text.
 * For text-input types the form sends the literal string the user typed.
 */
public final class AnswerFormatter {

    private AnswerFormatter() {}

    /** What the user submitted, rendered as a string. Empty string if blank. */
    public static String formatUserAnswer(Question q, String[] response) {
        if (response == null || response.length == 0) return "";
        if (q instanceof MultipleChoiceQuestion) {
            return lookupChoice(q, response[0]);
        }
        if (q instanceof MultiSelectQuestion) {
            List<String> parts = new ArrayList<>();
            for (String selected : response) {
                String text = lookupChoice(q, selected);
                if (!text.isEmpty()) parts.add(text);
            }
            return String.join(" | ", parts);
        }
        if (response.length == 1) return safe(response[0]);
        // multi-input questions (extension): join with " | "
        List<String> parts = new ArrayList<>(response.length);
        for (String r : response) parts.add(safe(r));
        return String.join(" | ", parts);
    }

    /** The correct answer(s), rendered as a string for display. */
    public static String formatCorrectAnswer(Question q) {
        if (q instanceof MultipleChoiceQuestion) {
            for (Answer a : q.getAnswers()) {
                if (a.isCorrect()) return safe(a.getText());
            }
            return "";
        }
        // text types and multi-select: list all correct answers separated by " / "
        List<String> texts = new ArrayList<>();
        for (Answer a : q.getAnswers()) {
            if (a.isCorrect()) texts.add(safe(a.getText()));
        }
        return String.join(" / ", texts);
    }

    private static String lookupChoice(Question q, String idRaw) {
        if (idRaw == null) return "";
        int id;
        try { id = Integer.parseInt(idRaw.trim()); } catch (NumberFormatException e) { return ""; }
        for (Answer a : q.getAnswers()) {
            if (a.getId() == id) return safe(a.getText());
        }
        return "";
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
