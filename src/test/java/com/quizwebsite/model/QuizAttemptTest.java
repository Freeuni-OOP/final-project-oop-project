package com.quizwebsite.model;

import com.quizwebsite.model.question.Answer;
import com.quizwebsite.model.question.Question;
import com.quizwebsite.model.question.QuestionResponseQuestion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizAttemptTest {

    @Test
    void practiceModeRepeatsQuestionsUntilEachIsAnsweredCorrectlyThreeTimes() {
        Quiz quiz = new Quiz();
        quiz.setName("Practice quiz");
        QuestionResponseQuestion first = question(1, "First?", "alpha");
        QuestionResponseQuestion second = question(2, "Second?", "beta");
        quiz.addQuestion(first);
        quiz.addQuestion(second);

        QuizAttempt attempt = new QuizAttempt(quiz, false, true);

        assertEquals(first, attempt.currentQuestion());
        answerCurrentCorrectly(attempt);
        assertEquals(second, attempt.currentQuestion());
        assertEquals(2, attempt.getPracticeRemaining());
        assertEquals(1, attempt.getPracticeCorrectCount(first.getId()));

        answerCurrentCorrectly(attempt);
        answerCurrentCorrectly(attempt);
        answerCurrentCorrectly(attempt);
        answerCurrentCorrectly(attempt);
        assertEquals(1, attempt.getPracticeRemaining());
        assertEquals(1, attempt.getPracticeMastered());
        assertFalse(attempt.isFinished());

        answerCurrentCorrectly(attempt);
        assertEquals(0, attempt.getPracticeRemaining());
        assertEquals(2, attempt.getPracticeMastered());
        assertTrue(attempt.isFinished());
    }

    private void answerCurrentCorrectly(QuizAttempt attempt) {
        Question question = attempt.currentQuestion();
        int score = question.getMaxScore();
        attempt.recordResponse(question.getId(), new String[] {"correct"}, score);
        attempt.recordPracticeResult(question, score);
    }

    private QuestionResponseQuestion question(Integer id, String body, String answerText) {
        QuestionResponseQuestion question = new QuestionResponseQuestion();
        question.setId(id);
        question.setBody(body);
        question.addAnswer(new Answer(answerText, true, 0));
        return question;
    }
}
