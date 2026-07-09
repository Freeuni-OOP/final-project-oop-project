package com.quizwebsite.repository;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.activity.QuizFavorite;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizFavoriteRepository extends JpaRepository<QuizFavorite, Integer> {

    boolean existsByUserIdAndQuizId(Integer userId, Integer quizId);

    Optional<QuizFavorite> findByUserIdAndQuizId(Integer userId, Integer quizId);

    long countByQuizId(Integer quizId);

    @Query("SELECT f.quiz FROM QuizFavorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Quiz> savedQuizzes(@Param("userId") Integer userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM QuizFavorite f WHERE f.quiz.id = :quizId")
    int deleteByQuizId(@Param("quizId") Integer quizId);

    @Modifying
    @Query("DELETE FROM QuizFavorite f WHERE f.user.id = :userId")
    int deleteByUserId(@Param("userId") Integer userId);
}
