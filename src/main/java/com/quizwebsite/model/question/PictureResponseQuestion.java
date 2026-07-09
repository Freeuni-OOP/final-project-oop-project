package com.quizwebsite.model.question;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Same text grading as Question-Response, but the prompt is an image (referenced
 * by absolute URL on {@link #getImageUrl()} per the spec — we don't host the bytes).
 */
@Entity
@DiscriminatorValue(PictureResponseQuestion.TYPE)
public class PictureResponseQuestion extends QuestionResponseQuestion {

    public static final String TYPE = "PICTURE_RESPONSE";

    @Override
    @Transient
    public String getType() { return TYPE; }
}
