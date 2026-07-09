package com.quizwebsite.service;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.QuizStatistics;
import com.quizwebsite.model.ReportStatus;
import com.quizwebsite.model.User;
import com.quizwebsite.model.question.Answer;
import com.quizwebsite.model.question.MultiSelectQuestion;
import com.quizwebsite.model.question.Question;
import com.quizwebsite.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizServiceTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private XmlImportService xmlImportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void addQuestionPersistsMultiSelectQuestionAndAnswersWithQuiz() {
        User creator = createUser("quiz_creator");
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName("Java Basics");
        quiz.setDescription("Intro quiz");
        Quiz savedQuiz = quizService.create(quiz);

        Question question = new MultiSelectQuestion();
        question.setBody("Select Java keywords.");
        question.addAnswer(new Answer("class", true, 0));
        question.addAnswer(new Answer("banana", false, 1));
        question.addAnswer(new Answer("public", true, 2));

        quizService.addQuestion(savedQuiz, question);
        entityManager.flush();
        entityManager.clear();

        Quiz reloaded = quizService.findByIdFullyLoaded(savedQuiz.getId()).orElseThrow();
        assertEquals(1, reloaded.getQuestions().size());

        Question reloadedQuestion = reloaded.getQuestions().getFirst();
        assertInstanceOf(MultiSelectQuestion.class, reloadedQuestion);
        assertEquals("Select Java keywords.", reloadedQuestion.getBody());
        assertEquals(0, reloadedQuestion.getPosition());
        assertEquals(3, reloadedQuestion.getAnswers().size());
        assertEquals(2, reloadedQuestion.getMaxScore());
        assertTrue(reloadedQuestion.getAnswers().getFirst().isCorrect());
    }

    @Test
    void listRecentReturnsNewestQuizzesFirstWithinLimit() {
        User creator = createUser("recent_creator");
        createQuiz("Old quiz", creator, LocalDateTime.now().minusDays(2));
        createQuiz("Newest quiz", creator, LocalDateTime.now());
        createQuiz("Middle quiz", creator, LocalDateTime.now().minusDays(1));
        entityManager.flush();
        entityManager.clear();

        List<Quiz> recent = quizService.listRecent(2);

        assertEquals(2, recent.size());
        assertEquals("Newest quiz", recent.get(0).getName());
        assertEquals("Middle quiz", recent.get(1).getName());
    }

    @Test
    void labelsFavoritesRatingsAndReportsRoundTrip() {
        User creator = createUser("extension_creator");
        User reviewer = createUser("extension_reviewer");
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName("Extension quiz");

        Quiz saved = quizService.create(quiz, "science", "java, oop");
        quizService.setFavorite(saved.getId(), reviewer.getId(), true);
        quizService.rateQuiz(saved.getId(), reviewer.getId(), 4, "Nice review");
        quizService.reportQuiz(saved.getId(), reviewer.getId(), "Needs admin review");
        entityManager.flush();
        entityManager.clear();

        Quiz reloaded = quizService.findById(saved.getId()).orElseThrow();
        assertEquals("Science", reloaded.getCategoryName());
        assertEquals("Java, Oop", reloaded.getTagsText());
        assertTrue(quizService.isFavorite(saved.getId(), reviewer.getId()));
        assertEquals(1, quizService.favoriteCount(saved.getId()));
        assertEquals(4.0, quizService.averageRating(saved.getId()));
        assertEquals("Nice review", quizService.ratingsFor(saved.getId()).getFirst().getReview());
        assertEquals(1, quizService.openReports().size());

        quizService.resolveReport(quizService.openReports().getFirst().getId(), ReportStatus.RESOLVED);
        assertEquals(0, quizService.openReportCount());
    }

    @Test
    void xmlImportCreatesQuizWithLabelsAndQuestions() {
        User creator = createUser("xml_creator");
        String xml = """
                <quiz title="Imported Quiz" category="demo" tags="xml, sample" random="true" pageMode="multiple">
                    <description>Imported description</description>
                    <question type="QUESTION_RESPONSE">
                        <prompt>Who?</prompt>
                        <answer>JFK</answer>
                    </question>
                    <question type="MULTI_SELECT">
                        <prompt>Select true.</prompt>
                        <choice>Water is H2O</choice>
                        <choice>The Moon is a star</choice>
                        <correctChoice>Water is H2O</correctChoice>
                    </question>
                </quiz>
                """;

        Quiz imported = xmlImportService.importQuiz(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), creator);
        entityManager.flush();
        entityManager.clear();

        Quiz reloaded = quizService.findByIdFullyLoaded(imported.getId()).orElseThrow();
        assertEquals("Imported Quiz", reloaded.getName());
        assertEquals("Demo", reloaded.getCategoryName());
        assertEquals("Sample, Xml", reloaded.getTagsText());
        assertEquals(2, reloaded.getQuestions().size());
        assertInstanceOf(MultiSelectQuestion.class, reloaded.getQuestions().get(1));
    }

    @Test
    void updateMetadataChangesLabelsAndOptions() {
        User creator = createUser("metadata_creator");
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName("Original");
        Quiz saved = quizService.create(quiz);

        quizService.updateMetadata(saved.getId(), "Updated", "New description", "math", "algebra, school",
                true, true, true, true);
        entityManager.flush();
        entityManager.clear();

        Quiz reloaded = quizService.findById(saved.getId()).orElseThrow();
        assertEquals("Updated", reloaded.getName());
        assertEquals("New description", reloaded.getDescription());
        assertEquals("Math", reloaded.getCategoryName());
        assertEquals("Algebra, School", reloaded.getTagsText());
        assertTrue(reloaded.isRandomQuestions());
        assertTrue(reloaded.isMultiPage());
        assertTrue(reloaded.isImmediateCorrection());
        assertTrue(reloaded.isPracticeEnabled());
    }

    @Test
    void searchFiltersQuizzesByQueryCategoryAndTag() {
        User creator = createUser("search_creator");
        Quiz target = new Quiz();
        target.setCreator(creator);
        target.setName("Java Interfaces");
        target.setDescription("Interfaces and polymorphism");
        quizService.create(target, "science", "java, oop");

        Quiz wrongCategory = new Quiz();
        wrongCategory.setCreator(creator);
        wrongCategory.setName("Java History");
        wrongCategory.setDescription("Interfaces in old technology");
        quizService.create(wrongCategory, "history", "java");

        Quiz wrongTag = new Quiz();
        wrongTag.setCreator(creator);
        wrongTag.setName("Physics Interfaces");
        wrongTag.setDescription("Interfaces between materials");
        quizService.create(wrongTag, "science", "physics");
        entityManager.flush();
        entityManager.clear();

        List<Quiz> filtered = quizService.search("interfaces", "science", "java", 10);

        assertEquals(1, filtered.size());
        assertEquals("Java Interfaces", filtered.getFirst().getName());
    }

    @Test
    void searchReturnsAvailableCategoriesAndTagsAlphabetically() {
        User creator = createUser("labels_browser");
        createLabeledQuiz("Algebra", creator, "math", "school, algebra");
        createLabeledQuiz("Biology", creator, "science", "cells");
        entityManager.flush();
        entityManager.clear();

        assertEquals(List.of("Math", "Science"), quizService.listCategories().stream()
                .map(category -> category.getName())
                .toList());
        assertEquals(Set.of("Algebra", "Cells", "School"), quizService.listTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toSet()));
    }

    @Test
    void statsForQuizSummarizesRecordedAttempts() {
        User creator = createUser("stats_creator");
        User taker = createUser("stats_taker");
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName("Stats quiz");
        Quiz saved = quizService.create(quiz);

        historyService.record(taker.getId(), saved.getId(), 4, 5, 60);
        historyService.record(taker.getId(), saved.getId(), 2, 5, 120);
        entityManager.flush();
        entityManager.clear();

        QuizStatistics stats = historyService.statsForQuiz(saved.getId());

        assertEquals(2, stats.getAttemptCount());
        assertEquals(3.0, stats.getAverageScore(), 0.001);
        assertEquals(60.0, stats.getAveragePercent(), 0.001);
        assertEquals(90.0, stats.getAverageTimeSeconds(), 0.001);
        assertEquals(4, stats.getBestScore());
    }

    @Test
    void questionDeleteAndMoveMaintainPositions() {
        User creator = createUser("question_editor");
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName("Question edit quiz");
        Quiz saved = quizService.create(quiz);

        Question first = new MultiSelectQuestion();
        first.setBody("First");
        first.addAnswer(new Answer("A", true, 0));
        first.addAnswer(new Answer("B", false, 1));
        Question second = new MultiSelectQuestion();
        second.setBody("Second");
        second.addAnswer(new Answer("C", true, 0));
        second.addAnswer(new Answer("D", false, 1));
        quizService.addQuestion(saved, first);
        quizService.addQuestion(saved, second);
        entityManager.flush();
        entityManager.clear();

        Quiz persisted = quizService.findByIdFullyLoaded(saved.getId()).orElseThrow();
        Integer secondId = persisted.getQuestions().stream()
                .filter(question -> "Second".equals(question.getBody()))
                .findFirst()
                .orElseThrow()
                .getId();

        quizService.moveQuestion(saved.getId(), secondId, -1);
        entityManager.flush();
        entityManager.clear();

        Quiz moved = quizService.findByIdFullyLoaded(saved.getId()).orElseThrow();
        assertEquals("Second", moved.getQuestions().get(0).getBody());
        assertEquals(0, moved.getQuestions().get(0).getPosition());
        assertEquals(1, moved.getQuestions().get(1).getPosition());

        quizService.deleteQuestion(saved.getId(), moved.getQuestions().get(0).getId());
        entityManager.flush();
        entityManager.clear();

        Quiz deleted = quizService.findByIdFullyLoaded(saved.getId()).orElseThrow();
        assertEquals(1, deleted.getQuestions().size());
        assertEquals("First", deleted.getQuestions().getFirst().getBody());
        assertEquals(0, deleted.getQuestions().getFirst().getPosition());
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setSalt("00112233445566778899aabbccddeeff");
        user.setPasswordHash("hash");
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private Quiz createQuiz(String name, User creator, LocalDateTime createdAt) {
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName(name);
        quiz.setDescription(name + " description");
        quiz.setCreatedAt(createdAt);
        return quizService.create(quiz);
    }

    private Quiz createLabeledQuiz(String name, User creator, String category, String tags) {
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName(name);
        return quizService.create(quiz, category, tags);
    }
}
