package com.quizwebsite.service;

import com.quizwebsite.model.Answer;
import com.quizwebsite.model.Question;
import com.quizwebsite.model.Quiz;
import com.quizwebsite.repository.QuizRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Handles loading and saving quizzes. */
@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Quiz> findById(int id) {
        return quizRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Quiz> listRecent(int limit) {
        return quizRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional
    public Quiz create(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    /** Adds a question to a quiz and saves it together with its answers. */
    @Transactional
    public void addQuestion(Quiz quiz, Question question) {
        quiz.addQuestion(question);
        for (Answer a : question.getAnswers()) {
            a.setQuestion(question);
        }
        quizRepository.save(quiz);
    }
}
