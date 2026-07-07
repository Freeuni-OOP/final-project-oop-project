package com.quizwebsite.service;

import com.quizwebsite.model.Answer;
import com.quizwebsite.model.MultipleChoiceQuestion;
import com.quizwebsite.model.Question;
import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizServiceTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void addQuestionPersistsQuestionAndAnswersWithQuiz() {
        User creator = createUser("quiz_creator");
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName("Java Basics");
        quiz.setDescription("Intro quiz");
        Quiz savedQuiz = quizService.create(quiz);

        Question question = new MultipleChoiceQuestion();
        question.setBody("What does JVM stand for?");
        question.addAnswer(new Answer("Java Virtual Machine", true, 0));
        question.addAnswer(new Answer("Java Visual Model", false, 1));

        quizService.addQuestion(savedQuiz, question);
        entityManager.flush();
        entityManager.clear();

        Quiz reloaded = quizService.findById(savedQuiz.getId()).orElseThrow();
        assertEquals(1, reloaded.getQuestions().size());

        Question reloadedQuestion = reloaded.getQuestions().getFirst();
        assertInstanceOf(MultipleChoiceQuestion.class, reloadedQuestion);
        assertEquals("What does JVM stand for?", reloadedQuestion.getBody());
        assertEquals(0, reloadedQuestion.getPosition());
        assertEquals(2, reloadedQuestion.getAnswers().size());
        assertTrue(reloadedQuestion.getAnswers().getFirst().isCorrect());
        assertEquals("Java Virtual Machine", reloadedQuestion.getAnswers().getFirst().getText());
    }

    @Test
    void listRecentReturnsNewestQuizzesFirstWithinLimit() {
        User creator = createUser("recent_creator");
        createQuiz("Old quiz", creator, LocalDateTime.now().minusDays(2));
        createQuiz("Newest quiz", creator, LocalDateTime.now());
        createQuiz("Middle quiz", creator, LocalDateTime.now().minusDays(1));
        entityManager.flush();
        entityManager.clear();

        List<Quiz> recent = quizService.listRecent(2);

        assertEquals(2, recent.size());
        assertEquals("Newest quiz", recent.get(0).getName());
        assertEquals("Middle quiz", recent.get(1).getName());
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setSalt("00112233445566778899aabbccddeeff");
        user.setPasswordHash("hash");
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private void createQuiz(String name, User creator, LocalDateTime createdAt) {
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName(name);
        quiz.setDescription(name + " description");
        quiz.setCreatedAt(createdAt);
        quizService.create(quiz);
    }
}
