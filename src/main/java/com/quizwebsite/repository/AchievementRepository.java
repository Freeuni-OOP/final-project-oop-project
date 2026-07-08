package com.quizwebsite.repository;

import com.quizwebsite.model.Achievement;
import com.quizwebsite.model.AchievementActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Integer> {

    boolean existsByUserIdAndKind(Integer userId, String kind);

    List<Achievement> findByUserIdOrderByAwardedAtDesc(Integer userId);

    @Query("SELECT new com.quizwebsite.model.AchievementActivity(u.id, u.username, a.kind, a.awardedAt) " +
           "FROM Achievement a JOIN User u ON u.id = a.userId " +
           "WHERE a.userId IN :userIds ORDER BY a.awardedAt DESC")
    List<AchievementActivity> recentForUsers(@Param("userIds") List<Integer> userIds, Pageable pageable);

    long countByUserId(Integer userId);

    @Modifying
    @Query("DELETE FROM Achievement a WHERE a.userId = :userId")
    int deleteByUserId(@Param("userId") Integer userId);
}
