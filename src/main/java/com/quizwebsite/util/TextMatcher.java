package com.quizwebsite.util;

import java.util.List;

/**
 * Text-answer comparison shared by every question type that grades free-text input
 * (Question-Response, Fill-in-the-Blank, Picture-Response, Multi-Answer).
 *
 * Matching is intentionally lenient — the spec wants "George Washington" and
 * "george washington" and " Washington " to all be acceptable when the creator
 * listed "Washington" or "George Washington" as legal answers.
 */
public final class TextMatcher {

    private TextMatcher() {}

    /** Lower-cases, trims, and collapses internal whitespace runs to one space. */
    public static String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /** Returns true if {@code userInput} matches any string in {@code acceptable} (after normalize). */
    public static boolean matchesAny(String userInput, List<String> acceptable) {
        String n = normalize(userInput);
        for (String a : acceptable) {
            if (normalize(a).equals(n)) return true;
        }
        return false;
    }
}
