package com.quizwebsite.repository;

import com.quizwebsite.model.social.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.toUser.id = :userId ORDER BY m.createdAt DESC")
    List<Message> inbox(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser.id = :userId AND m.read = false")
    long countByToUserIdAndReadFalse(@Param("userId") Integer userId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.fromUser.id = :fromId AND m.toUser.id = :toId AND m.type = :type")
    int deleteByFromToAndType(@Param("fromId") Integer fromId, @Param("toId") Integer toId, @Param("type") String type);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.fromUser.id = :userId OR m.toUser.id = :userId")
    int deleteAllForUser(@Param("userId") Integer userId);

    /** Detach challenge messages from a quiz about to be deleted (old ON DELETE SET NULL). */
    @Modifying
    @Query("UPDATE Message m SET m.quiz = null WHERE m.quiz.id = :quizId")
    int detachFromQuiz(@Param("quizId") Integer quizId);
}
