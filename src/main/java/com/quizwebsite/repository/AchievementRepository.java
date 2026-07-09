package com.quizwebsite.repository;

import com.quizwebsite.model.achievement.Achievement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Integer> {

    boolean existsByUserIdAndKind(Integer userId, String kind);

    List<Achievement> findByUserIdOrderByAwardedAtDesc(Integer userId);

    List<Achievement> findByUserIdInOrderByAwardedAtDesc(List<Integer> userIds, Pageable pageable);

    long countByUserId(Integer userId);

    @Modifying
    @Query("DELETE FROM Achievement a WHERE a.userId = :userId")
    int deleteByUserId(@Param("userId") Integer userId);
}
