package com.quizwebsite.service;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.repository.QuizRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Loads and saves quizzes. */
@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Transactional(readOnly = true)
    public List<Quiz> listRecent(int limit) {
        return quizRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional
    public Quiz create(Quiz quiz) {
        return quizRepository.save(quiz);
    }
}
