package com.quizwebsite.service;

import com.quizwebsite.model.User;
import com.quizwebsite.model.achievement.Achievement;
import com.quizwebsite.model.achievement.AchievementActivity;
import com.quizwebsite.model.achievement.AchievementKind;
import com.quizwebsite.repository.AchievementRepository;
import com.quizwebsite.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Reads and writes the achievements table. */
@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public AchievementService(AchievementRepository achievementRepository, UserRepository userRepository) {
        this.achievementRepository = achievementRepository;
        this.userRepository = userRepository;
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

        List<Achievement> achievements =
                achievementRepository.findByUserIdInOrderByAwardedAtDesc(userIds, PageRequest.of(0, limit));
        Map<Integer, String> usernames = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return achievements.stream()
                .map(a -> new AchievementActivity(a.getUserId(), usernames.get(a.getUserId()), a.getKind(), a.getAwardedAt()))
                .toList();
    }
}
