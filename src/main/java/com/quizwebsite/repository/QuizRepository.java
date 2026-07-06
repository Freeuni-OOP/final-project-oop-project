package com.quizwebsite.repository;

import com.quizwebsite.model.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    List<Quiz> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
