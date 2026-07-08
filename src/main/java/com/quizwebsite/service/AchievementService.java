package com.quizwebsite.service;

import com.quizwebsite.model.Achievement;
import com.quizwebsite.model.AchievementActivity;
import com.quizwebsite.model.AchievementKind;
import com.quizwebsite.repository.AchievementRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reads and writes the achievements table. */
@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;

    public AchievementService(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    /**
     * Awards the badge if the user doesn't already have it. Safe to call repeatedly
     * (the UNIQUE (user_id, kind) constraint backs it up). Returns true if newly awarded.
     */
    @Transactional
    public boolean awardIfMissing(int userId, AchievementKind kind) {
        if (achievementRepository.existsByUserIdAndKind(userId, kind.name())) {
            return false;
        }
        try {
            achievementRepository.save(new Achievement(userId, kind.name()));
            return true;
        } catch (DataIntegrityViolationException race) {
            return false; // lost a race on the unique constraint — already awarded
        }
    }

    @Transactional(readOnly = true)
    public List<Achievement> listForUser(int userId) {
        return achievementRepository.findByUserIdOrderByAwardedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long countForUser(int userId) {
        return achievementRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<AchievementActivity> recentForUsers(List<Integer> userIds, int limit) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return achievementRepository.recentForUsers(userIds, PageRequest.of(0, limit));
    }
}
