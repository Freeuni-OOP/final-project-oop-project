package com.quizwebsite.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** A question where the user picks one of the given choices. */
@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceQuestion extends Question {
}
