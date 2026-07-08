package com.quizwebsite.model;

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
}
