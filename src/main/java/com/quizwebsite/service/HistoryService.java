package com.quizwebsite.service;

import com.quizwebsite.model.QuizHistoryEntry;
import com.quizwebsite.model.QuizStatistics;
import com.quizwebsite.repository.HistoryRepository;
import com.quizwebsite.repository.QuizRepository;
import com.quizwebsite.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Reads and writes quiz_history (leaderboards, per-user history, stats). */
@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public HistoryService(HistoryRepository historyRepository,
                          UserRepository userRepository,
                          QuizRepository quizRepository) {
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    @Transactional
    public void record(int userId, int quizId, int score, int maxScore, int seconds) {
        QuizHistoryEntry entry = new QuizHistoryEntry(
                userRepository.getReferenceById(userId),
                quizRepository.getReferenceById(quizId),
                score, maxScore, seconds);
        historyRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryEntry> topAllTime(int quizId, int limit) {
        return historyRepository.topAllTime(quizId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryEntry> topInLastDay(int quizId, int limit) {
        return historyRepository.topInLastDay(quizId, LocalDateTime.now().minusDays(1), PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryEntry> recentForQuiz(int quizId, int limit) {
        return historyRepository.recentForQuiz(quizId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public QuizStatistics statsForQuiz(int quizId) {
        List<QuizHistoryEntry> attempts = historyRepository.listForQuiz(quizId);
        if (attempts.isEmpty()) {
            return new QuizStatistics(0, 0.0, 0.0, 0.0, 0);
        }
        double averageScore = attempts.stream().mapToInt(QuizHistoryEntry::getScore).average().orElse(0.0);
        double averagePercent = attempts.stream()
                .mapToDouble(entry -> entry.getMaxScore() == 0 ? 0.0 : (100.0 * entry.getScore()) / entry.getMaxScore())
                .average()
                .orElse(0.0);
        double averageTime = attempts.stream().mapToInt(QuizHistoryEntry::getTimeTakenSeconds).average().orElse(0.0);
        int bestScore = attempts.stream().mapToInt(QuizHistoryEntry::getScore).max().orElse(0);
        return new QuizStatistics(attempts.size(), averageScore, averagePercent, averageTime, bestScore);
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryEntry> listForUserOnQuiz(int userId, int quizId) {
        return historyRepository.listForUserOnQuiz(userId, quizId);
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryEntry> listForUser(int userId, int limit) {
        return historyRepository.listForUser(userId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<QuizHistoryEntry> listForUsers(List<Integer> userIds, int limit) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return historyRepository.listForUsers(userIds, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public long countAttemptsForUser(int userId) {
        return historyRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countAttemptsForQuiz(int quizId) {
        return historyRepository.countByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public long countAttemptsAll() {
        return historyRepository.count();
    }

    @Transactional
    public int clearForQuiz(int quizId) {
        return historyRepository.deleteByQuizId(quizId);
    }
}
