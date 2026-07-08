package com.quizwebsite.service;

import com.quizwebsite.model.AchievementKind;
import com.quizwebsite.model.QuizHistoryEntry;
import com.quizwebsite.repository.HistoryRepository;
import com.quizwebsite.repository.QuizRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Rule engine that turns "the user just did X" events into achievement awards.
 *
 * Callers only fire the right event; the engine decides what to award. Adding a new
 * achievement is: add an enum constant in {@link AchievementKind} and a check here —
 * controllers stay untouched.
 */
@Service
public class AchievementEngine {

    private final AchievementService achievementService;
    private final QuizRepository quizRepository;
    private final HistoryRepository historyRepository;

    public AchievementEngine(AchievementService achievementService,
                             QuizRepository quizRepository,
                             HistoryRepository historyRepository) {
        this.achievementService = achievementService;
        this.quizRepository = quizRepository;
        this.historyRepository = historyRepository;
    }

    /** Fire after a user creates a new quiz. */
    @Transactional
    public void onQuizCreated(int userId) {
        long created = quizRepository.countByCreatorId(userId);
        if (created >= 1)  achievementService.awardIfMissing(userId, AchievementKind.AMATEUR_AUTHOR);
        if (created >= 5)  achievementService.awardIfMissing(userId, AchievementKind.PROLIFIC_AUTHOR);
        if (created >= 10) achievementService.awardIfMissing(userId, AchievementKind.PRODIGIOUS_AUTHOR);
    }

    /**
     * Fire after a user finishes a quiz attempt (practice or real).
     *
     * Practice attempts award only PRACTICE_MAKES_PERFECT; the "took quizzes" badges
     * count rows in quiz_history, where practice attempts are not recorded.
     */
    @Transactional
    public void onQuizCompleted(int userId, int quizId, boolean practice) {
        if (practice) {
            achievementService.awardIfMissing(userId, AchievementKind.PRACTICE_MAKES_PERFECT);
            return;
        }
        long attempts = historyRepository.countByUserId(userId);
        if (attempts >= 10) achievementService.awardIfMissing(userId, AchievementKind.QUIZ_MACHINE);

        // I am the Greatest — awarded once when you currently top this quiz's leaderboard.
        // Permanent: it doesn't go away when someone later beats the score.
        if (userHoldsHighScore(userId, quizId)) {
            achievementService.awardIfMissing(userId, AchievementKind.I_AM_THE_GREATEST);
        }
    }

    private boolean userHoldsHighScore(int userId, int quizId) {
        List<QuizHistoryEntry> top = historyRepository.rankedForQuiz(quizId, PageRequest.of(0, 1));
        return !top.isEmpty() && top.get(0).getUserId() != null && top.get(0).getUserId() == userId;
    }
}
