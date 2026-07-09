package com.quizwebsite.service;

import com.quizwebsite.model.Category;
import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.QuizFavorite;
import com.quizwebsite.model.QuizRating;
import com.quizwebsite.model.QuizReport;
import com.quizwebsite.model.ReportStatus;
import com.quizwebsite.model.Tag;
import com.quizwebsite.model.question.Answer;
import com.quizwebsite.model.question.Question;
import com.quizwebsite.repository.CategoryRepository;
import com.quizwebsite.repository.HistoryRepository;
import com.quizwebsite.repository.MessageRepository;
import com.quizwebsite.repository.QuizFavoriteRepository;
import com.quizwebsite.repository.QuizRatingRepository;
import com.quizwebsite.repository.QuizReportRepository;
import com.quizwebsite.repository.QuizRepository;
import com.quizwebsite.repository.TagRepository;
import com.quizwebsite.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Loads and saves quizzes plus their nested questions and answers. */
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final HistoryRepository historyRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final QuizFavoriteRepository favoriteRepository;
    private final QuizRatingRepository ratingRepository;
    private final QuizReportRepository reportRepository;

    public QuizService(QuizRepository quizRepository,
                       HistoryRepository historyRepository,
                       MessageRepository messageRepository,
                       UserRepository userRepository,
                       CategoryRepository categoryRepository,
                       TagRepository tagRepository,
                       QuizFavoriteRepository favoriteRepository,
                       QuizRatingRepository ratingRepository,
                       QuizReportRepository reportRepository) {
        this.quizRepository = quizRepository;
        this.historyRepository = historyRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Quiz> findById(int id) {
        return quizRepository.findById(id);
    }

    /**
     * Loads a quiz with its full question + answer graph fully initialized, so it can
     * be safely stored in the session for a quiz attempt (no lazy loading later).
     */
    @Transactional(readOnly = true)
    public Optional<Quiz> findByIdFullyLoaded(int id) {
        Optional<Quiz> opt = quizRepository.findByIdWithQuestions(id);
        opt.ifPresent(quiz -> {
            Hibernate.initialize(quiz.getCreator());
            for (Question q : quiz.getQuestions()) {
                Hibernate.initialize(q.getAnswers());
            }
        });
        return opt;
    }

    @Transactional(readOnly = true)
    public List<Quiz> listRecent(int limit) {
        return quizRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Quiz> search(String query, String category, String tag, int limit) {
        return quizRepository.search(normalizeFilter(query), normalizeFilter(category), normalizeFilter(tag),
                PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Quiz> listPopular(int limit) {
        List<Quiz> popular = new ArrayList<>(historyRepository.popularQuizzes(PageRequest.of(0, limit)));
        Set<Integer> seen = new LinkedHashSet<>();
        for (Quiz quiz : popular) seen.add(quiz.getId());
        for (Quiz quiz : listRecent(limit)) {
            if (popular.size() >= limit) break;
            if (seen.add(quiz.getId())) popular.add(quiz);
        }
        return popular;
    }

    @Transactional(readOnly = true)
    public List<Quiz> listByCreator(int creatorId) {
        return quizRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId);
    }

    @Transactional(readOnly = true)
    public List<Quiz> listByCreators(List<Integer> creatorIds, int limit) {
        if (creatorIds == null || creatorIds.isEmpty()) return List.of();
        return quizRepository.findByCreatorIdInOrderByCreatedAtDesc(creatorIds, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Category> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Tag> listTags() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public long countByCreator(int creatorId) {
        return quizRepository.countByCreatorId(creatorId);
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return quizRepository.count();
    }

    /** Inserts the quiz and every nested question + answer in one transaction. */
    @Transactional
    public Quiz create(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz create(Quiz quiz, String categoryName, String tagsText) {
        applyLabels(quiz, categoryName, tagsText);
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz updateMetadata(int quizId,
                               String name,
                               String description,
                               String categoryName,
                               String tagsText,
                               boolean randomQuestions,
                               boolean multiPage,
                               boolean immediateCorrection,
                               boolean practiceEnabled) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw new IllegalArgumentException("Quiz name is required.");
        }
        quiz.setName(trimmedName);
        quiz.setDescription(description == null ? null : description.trim());
        quiz.setRandomQuestions(randomQuestions);
        quiz.setMultiPage(multiPage);
        quiz.setImmediateCorrection(immediateCorrection);
        quiz.setPracticeEnabled(practiceEnabled);
        applyLabels(quiz, categoryName, tagsText);
        return quizRepository.save(quiz);
    }

    /** Appends one question (with its answers) to an existing quiz. */
    @Transactional
    public void addQuestion(Quiz quiz, Question question) {
        if (question.getAnswerSlots() <= 0) {
            question.setAnswerSlots(question.getAnswers().size());
        }
        quiz.addQuestion(question);                 // wires back-ref + position
        for (Answer a : question.getAnswers()) {
            a.setQuestion(question);
        }
        quizRepository.save(quiz);                   // cascade persists the new question + answers
    }

    @Transactional
    public void deleteQuestion(int quizId, int questionId) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId).orElseThrow();
        quiz.getQuestions().removeIf(question -> question.getId() != null && question.getId() == questionId);
        renumberQuestions(quiz);
        quizRepository.save(quiz);
    }

    @Transactional
    public void moveQuestion(int quizId, int questionId, int delta) {
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId).orElseThrow();
        quiz.getQuestions().sort(Comparator.comparingInt(Question::getPosition));
        int index = -1;
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            Question q = quiz.getQuestions().get(i);
            if (q.getId() != null && q.getId() == questionId) {
                index = i;
                break;
            }
        }
        int target = index + delta;
        if (index < 0 || target < 0 || target >= quiz.getQuestions().size()) return;
        Question current = quiz.getQuestions().get(index);
        quiz.getQuestions().set(index, quiz.getQuestions().get(target));
        quiz.getQuestions().set(target, current);
        renumberQuestions(quiz);
        quizRepository.save(quiz);
    }

    /**
     * Deletes a quiz and everything that depends on it: questions + answers (cascade),
     * its quiz_history rows, and detaches any challenge messages that pointed at it.
     */
    @Transactional
    public void delete(int quizId) {
        messageRepository.detachFromQuiz(quizId);
        favoriteRepository.deleteByQuizId(quizId);
        ratingRepository.deleteByQuizId(quizId);
        reportRepository.deleteByQuizId(quizId);
        historyRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }

    @Transactional
    public void deleteUserExtensions(int userId) {
        favoriteRepository.deleteByUserId(userId);
        ratingRepository.deleteByReviewerId(userId);
        reportRepository.deleteByReporterId(userId);
    }

    @Transactional
    public void setFavorite(int quizId, int userId, boolean saved) {
        if (saved) {
            if (!favoriteRepository.existsByUserIdAndQuizId(userId, quizId)) {
                favoriteRepository.save(new QuizFavorite(
                        userRepository.getReferenceById(userId),
                        quizRepository.getReferenceById(quizId)));
            }
        } else {
            favoriteRepository.findByUserIdAndQuizId(userId, quizId).ifPresent(favoriteRepository::delete);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(int quizId, int userId) {
        return favoriteRepository.existsByUserIdAndQuizId(userId, quizId);
    }

    @Transactional(readOnly = true)
    public long favoriteCount(int quizId) {
        return favoriteRepository.countByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public List<Quiz> savedBy(int userId, int limit) {
        return favoriteRepository.savedQuizzes(userId, PageRequest.of(0, limit));
    }

    @Transactional
    public void rateQuiz(int quizId, int reviewerId, int rating, String review) {
        QuizRating quizRating = ratingRepository.findByQuizIdAndReviewerId(quizId, reviewerId)
                .orElseGet(() -> new QuizRating(
                        quizRepository.getReferenceById(quizId),
                        userRepository.getReferenceById(reviewerId),
                        rating,
                        review));
        quizRating.setRating(rating);
        quizRating.setReview(review == null || review.isBlank() ? null : review.trim());
        ratingRepository.save(quizRating);
    }

    @Transactional(readOnly = true)
    public List<QuizRating> ratingsFor(int quizId) {
        return ratingRepository.findByQuizIdOrderByCreatedAtDesc(quizId);
    }

    @Transactional(readOnly = true)
    public double averageRating(int quizId) {
        return ratingRepository.averageForQuiz(quizId);
    }

    @Transactional
    public void reportQuiz(int quizId, int reporterId, String reason) {
        String trimmed = reason == null ? "" : reason.trim();
        if (trimmed.isEmpty()) return;
        reportRepository.save(new QuizReport(
                quizRepository.getReferenceById(quizId),
                userRepository.getReferenceById(reporterId),
                trimmed));
    }

    @Transactional(readOnly = true)
    public List<QuizReport> openReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public long openReportCount() {
        return reportRepository.countByStatus(ReportStatus.OPEN);
    }

    @Transactional
    public void resolveReport(int reportId, ReportStatus status) {
        QuizReport report = reportRepository.findById(reportId).orElseThrow();
        report.resolve(status);
        reportRepository.save(report);
    }

    public void applyLabels(Quiz quiz, String categoryName, String tagsText) {
        quiz.setCategory(resolveCategory(categoryName));
        quiz.getTags().clear();
        for (String tagName : parseDelimited(tagsText)) {
            quiz.getTags().add(resolveTag(tagName));
        }
    }

    private Category resolveCategory(String categoryName) {
        String normalized = normalizeLabel(categoryName);
        if (normalized == null) return null;
        return categoryRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> categoryRepository.save(new Category(normalized)));
    }

    private Tag resolveTag(String tagName) {
        String normalized = normalizeLabel(tagName);
        if (normalized == null) throw new IllegalArgumentException("Blank tag");
        return tagRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> tagRepository.save(new Tag(normalized)));
    }

    private List<String> parseDelimited(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split(",")).stream()
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
    }

    private String normalizeLabel(String value) {
        if (value == null || value.isBlank()) return null;
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private void renumberQuestions(Quiz quiz) {
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            quiz.getQuestions().get(i).setPosition(i);
        }
    }
}
