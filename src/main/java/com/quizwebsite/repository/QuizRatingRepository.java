package com.quizwebsite.repository;

import com.quizwebsite.model.QuizRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRatingRepository extends JpaRepository<QuizRating, Integer> {

    Optional<QuizRating> findByQuizIdAndReviewerId(Integer quizId, Integer reviewerId);

    List<QuizRating> findByQuizIdOrderByCreatedAtDesc(Integer quizId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM QuizRating r WHERE r.quiz.id = :quizId")
    double averageForQuiz(@Param("quizId") Integer quizId);

    @Modifying
    @Query("DELETE FROM QuizRating r WHERE r.quiz.id = :quizId")
    int deleteByQuizId(@Param("quizId") Integer quizId);

    @Modifying
    @Query("DELETE FROM QuizRating r WHERE r.reviewer.id = :userId")
    int deleteByReviewerId(@Param("userId") Integer userId);
}
