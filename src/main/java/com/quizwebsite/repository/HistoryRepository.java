package com.quizwebsite.repository;

import com.quizwebsite.model.QuizHistoryEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoryRepository extends JpaRepository<QuizHistoryEntry, Integer> {

    /** Spec ranking: score DESC, then time_taken ASC (faster finisher wins ties). */
    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId " +
           "ORDER BY h.score DESC, h.timeTakenSeconds ASC")
    List<QuizHistoryEntry> topAllTime(@Param("quizId") Integer quizId, Pageable pageable);

    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId " +
           "AND h.takenAt >= :since " +
           "ORDER BY h.score DESC, h.timeTakenSeconds ASC")
    List<QuizHistoryEntry> topInLastDay(@Param("quizId") Integer quizId,
                                        @Param("since") java.time.LocalDateTime since,
                                        Pageable pageable);

    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId ORDER BY h.takenAt DESC")
    List<QuizHistoryEntry> recentForQuiz(@Param("quizId") Integer quizId, Pageable pageable);

    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId")
    List<QuizHistoryEntry> listForQuiz(@Param("quizId") Integer quizId);

    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.user.id = :userId AND h.quiz.id = :quizId " +
           "ORDER BY h.takenAt DESC")
    List<QuizHistoryEntry> listForUserOnQuiz(@Param("userId") Integer userId, @Param("quizId") Integer quizId);

    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.user.id = :userId ORDER BY h.takenAt DESC")
    List<QuizHistoryEntry> listForUser(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.user.id IN :userIds ORDER BY h.takenAt DESC")
    List<QuizHistoryEntry> listForUsers(@Param("userIds") List<Integer> userIds, Pageable pageable);

    @Query("SELECT h.quiz FROM QuizHistoryEntry h GROUP BY h.quiz ORDER BY COUNT(h) DESC, MAX(h.takenAt) DESC")
    List<com.quizwebsite.model.Quiz> popularQuizzes(Pageable pageable);

    @Query("SELECT COUNT(h) FROM QuizHistoryEntry h WHERE h.user.id = :userId")
    long countByUserId(@Param("userId") Integer userId);

    @Query("SELECT COUNT(h) FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId")
    long countByQuizId(@Param("quizId") Integer quizId);

    @Modifying
    @Query("DELETE FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId")
    int deleteByQuizId(@Param("quizId") Integer quizId);

    @Modifying
    @Query("DELETE FROM QuizHistoryEntry h WHERE h.user.id = :userId")
    int deleteByUserId(@Param("userId") Integer userId);

    /** The single best attempt on a quiz (score DESC, time ASC), as the leaderboard ranks it. */
    @Query("SELECT h FROM QuizHistoryEntry h WHERE h.quiz.id = :quizId " +
           "ORDER BY h.score DESC, h.timeTakenSeconds ASC")
    List<QuizHistoryEntry> rankedForQuiz(@Param("quizId") Integer quizId, Pageable pageable);

    /** A user's best score on a quiz (highest score), used to render challenge messages. */
    @Query("SELECT MAX(h.score) FROM QuizHistoryEntry h WHERE h.user.id = :userId AND h.quiz.id = :quizId")
    Integer bestScore(@Param("userId") Integer userId, @Param("quizId") Integer quizId);

    @Query("SELECT h.maxScore FROM QuizHistoryEntry h WHERE h.user.id = :userId AND h.quiz.id = :quizId " +
           "ORDER BY h.score DESC, h.timeTakenSeconds ASC")
    List<Integer> maxScoresForBest(@Param("userId") Integer userId, @Param("quizId") Integer quizId, Pageable pageable);
}
