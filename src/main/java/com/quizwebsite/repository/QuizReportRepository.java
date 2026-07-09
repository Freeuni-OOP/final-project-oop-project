package com.quizwebsite.repository;

import com.quizwebsite.model.activity.QuizReport;
import com.quizwebsite.model.activity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizReportRepository extends JpaRepository<QuizReport, Integer> {

    List<QuizReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    long countByStatus(ReportStatus status);

    @Modifying
    @Query("DELETE FROM QuizReport r WHERE r.quiz.id = :quizId")
    int deleteByQuizId(@Param("quizId") Integer quizId);

    @Modifying
    @Query("DELETE FROM QuizReport r WHERE r.reporter.id = :userId")
    int deleteByReporterId(@Param("userId") Integer userId);
}
