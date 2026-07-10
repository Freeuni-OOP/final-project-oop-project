package com.quizwebsite.model.question;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Same grading rules as {@link QuestionResponseQuestion}, but the question body
 * contains a literal blank marker. By convention the creator writes "____"
 * (four or more underscores) where the blank should appear. The renderer splits
 * on that marker to draw a text input inline.
 */
@Entity
@DiscriminatorValue(FillBlankQuestion.TYPE)
public class FillBlankQuestion extends QuestionResponseQuestion {

    public static final String TYPE = "FILL_BLANK";

    /** Marker used in {@link #getBody()} to indicate where the blank goes. */
    public static final String BLANK_TOKEN = "____";

    @Override
    @Transient
    public String getType() { return TYPE; }

    /** The question text before the blank marker (the whole body if there is none). */
    @Transient
    public String getTextBeforeBlank() {
        String body = getBody() == null ? "" : getBody();
        int i = body.indexOf(BLANK_TOKEN);
        return i < 0 ? body : body.substring(0, i);
    }

    /** The question text after the blank marker ("" if there is none). */
    @Transient
    public String getTextAfterBlank() {
        String body = getBody() == null ? "" : getBody();
        int i = body.indexOf(BLANK_TOKEN);
        if (i < 0) return "";
        int end = i + BLANK_TOKEN.length();
        while (end < body.length() && body.charAt(end) == '_') end++;  // creators may type extra underscores
        return body.substring(end);
    }
}
