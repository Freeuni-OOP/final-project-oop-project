package com.quizwebsite.model.question;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionGradingTest {

    @Test
    void textQuestionAcceptsAnyLegalAnswer() {
        Question question = new QuestionResponseQuestion();
        question.addAnswer(answer(1, "John F. Kennedy", true, 0));
        question.addAnswer(answer(2, "JFK", true, 1));

        assertEquals(1, question.grade(new String[] {" jfk "}));
        assertEquals(0, question.grade(new String[] {"Nixon"}));
    }

    @Test
    void fillBlankAndPictureResponseUseTextMatching() {
        Question fillBlank = new FillBlankQuestion();
        fillBlank.addAnswer(answer(3, "Mercury", true, 0));
        Question picture = new PictureResponseQuestion();
        picture.addAnswer(answer(4, "Golden Gate Bridge", true, 0));

        assertEquals(1, fillBlank.grade(new String[] {" mercury "}));
        assertEquals(1, picture.grade(new String[] {"golden gate bridge"}));
    }

    @Test
    void multipleChoiceRequiresCorrectChoiceId() {
        Question question = new MultipleChoiceQuestion();
        question.addAnswer(answer(10, "Earth", false, 0));
        question.addAnswer(answer(11, "Jupiter", true, 1));

        assertEquals(1, question.grade(new String[] {"11"}));
        assertEquals(0, question.grade(new String[] {"10"}));
    }

    @Test
    void unorderedMultiAnswerAwardsDistinctLegalAnswers() {
        Question question = new MultiAnswerQuestion();
        question.setOrdered(false);
        question.addAnswer(answer(20, "New York", true, 0));
        question.addAnswer(answer(21, "Los Angeles", true, 1));
        question.addAnswer(answer(22, "Chicago", true, 2));

        assertEquals(2, question.grade(new String[] {" chicago ", "New York", "Chicago", "Boston"}));
    }

    @Test
    void orderedMultiAnswerRequiresMatchingPositions() {
        Question question = new MultiAnswerQuestion();
        question.setOrdered(true);
        question.addAnswer(answer(30, "Mercury", true, 0));
        question.addAnswer(answer(31, "Venus", true, 1));

        assertEquals(2, question.grade(new String[] {"mercury", " venus "}));
        assertEquals(0, question.grade(new String[] {"venus", "mercury"}));
    }

    @Test
    void multiSelectAwardsCorrectSelectionsAndPenalizesWrongOnes() {
        Question question = new MultiSelectQuestion();
        question.addAnswer(answer(40, "Water is H2O", true, 0));
        question.addAnswer(answer(41, "The Moon is a star", false, 1));
        question.addAnswer(answer(42, "Stanford was established in 1891", true, 2));

        assertEquals(2, question.grade(new String[] {"40", "42"}));
        assertEquals(1, question.grade(new String[] {"40", "41", "42"}));
    }

    private Answer answer(Integer id, String text, boolean correct, int position) {
        Answer answer = new Answer(text, correct, position);
        answer.setId(id);
        return answer;
    }
}
