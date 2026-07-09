package com.quizwebsite.web;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.QuizAttempt;
import com.quizwebsite.model.User;
import com.quizwebsite.model.question.Answer;
import com.quizwebsite.model.question.FillBlankQuestion;
import com.quizwebsite.model.question.MultiAnswerQuestion;
import com.quizwebsite.model.question.MultiSelectQuestion;
import com.quizwebsite.model.question.MultipleChoiceQuestion;
import com.quizwebsite.model.question.PictureResponseQuestion;
import com.quizwebsite.model.question.Question;
import com.quizwebsite.model.question.QuestionResponseQuestion;
import com.quizwebsite.service.AchievementEngine;
import com.quizwebsite.service.HistoryService;
import com.quizwebsite.service.PrivacyService;
import com.quizwebsite.service.QuizService;
import com.quizwebsite.service.XmlImportService;
import com.quizwebsite.util.AnswerFormatter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Browsing, creating, editing, taking, and scoring quizzes. */
@Controller
@RequestMapping("/quizzes")
public class QuizController {

    private static final int RECENT_LIMIT = 50;
    private static final int LEADERBOARD_LIMIT = 10;
    private static final int RECENT_ATTEMPTS_LIMIT = 10;
    private static final int FIELDS_SHOWN = 5;

    private static final Set<String> VALID_TYPES = Set.of(
            QuestionResponseQuestion.TYPE, FillBlankQuestion.TYPE,
            MultipleChoiceQuestion.TYPE, PictureResponseQuestion.TYPE,
            MultiAnswerQuestion.TYPE, MultiSelectQuestion.TYPE);

    private final QuizService quizService;
    private final HistoryService historyService;
    private final AchievementEngine achievementEngine;
    private final XmlImportService xmlImportService;
    private final PrivacyService privacyService;

    public QuizController(QuizService quizService,
                          HistoryService historyService,
                          AchievementEngine achievementEngine,
                          XmlImportService xmlImportService,
                          PrivacyService privacyService) {
        this.quizService = quizService;
        this.historyService = historyService;
        this.achievementEngine = achievementEngine;
        this.xmlImportService = xmlImportService;
        this.privacyService = privacyService;
    }

    // ===================================================================
    // Browse
    // ===================================================================

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String tag,
                       Model model) {
        model.addAttribute("quizzes", quizService.search(q, category, tag, RECENT_LIMIT));
        model.addAttribute("categories", quizService.listCategories());
        model.addAttribute("tags", quizService.listTags());
        model.addAttribute("query", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedTag", tag);
        return "quizzes";
    }

    // ===================================================================
    // Create
    // ===================================================================

    @GetMapping("/new")
    public String createForm() {
        return "create-quiz";
    }

    @PostMapping("/new")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String categoryName,
                         @RequestParam(required = false) String tagsText,
                         @RequestParam(required = false) String randomQuestions,
                         @RequestParam(required = false) String multiPage,
                         @RequestParam(required = false) String immediateCorrection,
                         @RequestParam(required = false) String practiceEnabled,
                         HttpSession session,
                         Model model) {
        User user = SessionKeys.currentUser(session);
        String trimmedName = name == null ? null : name.trim();
        if (trimmedName == null || trimmedName.isEmpty()) {
            model.addAttribute("error", "Quiz name is required.");
            model.addAttribute("name", trimmedName);
            model.addAttribute("description", description);
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("tagsText", tagsText);
            return "create-quiz";
        }

        Quiz quiz = new Quiz();
        quiz.setCreator(user);
        quiz.setName(trimmedName);
        quiz.setDescription(description == null ? null : description.trim());
        quiz.setRandomQuestions("on".equals(randomQuestions));
        quiz.setMultiPage("on".equals(multiPage));
        quiz.setImmediateCorrection("on".equals(immediateCorrection));
        quiz.setPracticeEnabled("on".equals(practiceEnabled));

        Quiz saved = quizService.create(quiz, categoryName, tagsText);
        achievementEngine.onQuizCreated(user.getId());
        return "redirect:/quizzes/edit?id=" + saved.getId();
    }

    @PostMapping("/import-xml")
    public String importXml(@RequestParam("file") MultipartFile file,
                            HttpSession session,
                            Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Choose an XML file to import.");
            return "create-quiz";
        }
        try {
            User user = SessionKeys.currentUser(session);
            Quiz quiz = xmlImportService.importQuiz(file.getInputStream(), user);
            achievementEngine.onQuizCreated(user.getId());
            return "redirect:/quizzes/edit?id=" + quiz.getId();
        } catch (Exception ex) {
            model.addAttribute("error", "Invalid quiz XML.");
            return "create-quiz";
        }
    }

    // ===================================================================
    // Edit / add questions
    // ===================================================================

    @GetMapping("/edit")
    public String edit(@RequestParam int id, HttpSession session, Model model) {
        Quiz quiz = loadOwnedQuiz(id, session);
        model.addAttribute("quiz", quiz);
        return "edit-quiz";
    }

    @PostMapping("/edit")
    public String update(@RequestParam int id,
                         @RequestParam String name,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String categoryName,
                         @RequestParam(required = false) String tagsText,
                         @RequestParam(required = false) String randomQuestions,
                         @RequestParam(required = false) String multiPage,
                         @RequestParam(required = false) String immediateCorrection,
                         @RequestParam(required = false) String practiceEnabled,
                         HttpSession session,
                         Model model) {
        loadOwnedQuiz(id, session);
        try {
            quizService.updateMetadata(id, name, description, categoryName, tagsText,
                    "on".equals(randomQuestions),
                    "on".equals(multiPage),
                    "on".equals(immediateCorrection),
                    "on".equals(practiceEnabled));
            return "redirect:/quizzes/edit?id=" + id;
        } catch (IllegalArgumentException ex) {
            Quiz quiz = loadOwnedQuiz(id, session);
            model.addAttribute("quiz", quiz);
            model.addAttribute("error", ex.getMessage());
            return "edit-quiz";
        }
    }

    @PostMapping("/question-action")
    public String questionAction(@RequestParam int quizId,
                                 @RequestParam int questionId,
                                 @RequestParam String intent,
                                 HttpSession session) {
        loadOwnedQuiz(quizId, session);
        switch (intent) {
            case "delete" -> quizService.deleteQuestion(quizId, questionId);
            case "up" -> quizService.moveQuestion(quizId, questionId, -1);
            case "down" -> quizService.moveQuestion(quizId, questionId, 1);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown intent");
        }
        return "redirect:/quizzes/edit?id=" + quizId;
    }

    @GetMapping("/add-question")
    public String addQuestionForm(@RequestParam int quizId,
                                  @RequestParam String type,
                                  HttpSession session,
                                  Model model) {
        Quiz quiz = loadOwnedQuiz(quizId, session);
        requireValidType(type);
        model.addAttribute("quiz", quiz);
        model.addAttribute("type", type);
        model.addAttribute("fieldsShown", FIELDS_SHOWN);
        return "add-question";
    }

    @PostMapping("/add-question")
    public String addQuestion(@RequestParam int quizId,
                              @RequestParam String type,
                              @RequestParam(required = false) String body,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam(required = false) String ordered,
                              @RequestParam(name = "answer", required = false) List<String> answerValues,
                              @RequestParam(name = "choice", required = false) List<String> choiceValues,
                              @RequestParam(name = "correct", required = false) List<Integer> correctIndexes,
                              HttpSession session,
                              Model model) {
        Quiz quiz = loadOwnedQuiz(quizId, session);
        requireValidType(type);

        String trimmedBody = body == null ? null : body.trim();
        String trimmedImage = imageUrl == null ? null : imageUrl.trim();

        String error = null;
        if (trimmedBody == null || trimmedBody.isEmpty()) error = "Question text is required.";
        if (error == null && PictureResponseQuestion.TYPE.equals(type)
                && (trimmedImage == null || trimmedImage.isEmpty())) {
            error = "Picture-Response questions need an image URL.";
        }
        if (error == null && FillBlankQuestion.TYPE.equals(type)
                && !trimmedBody.contains(FillBlankQuestion.BLANK_TOKEN)) {
            error = "Fill-in-the-Blank questions must contain \"" + FillBlankQuestion.BLANK_TOKEN + "\" to mark the blank.";
        }

        List<Answer> answers;
        if (isChoiceType(type)) {
            answers = parseChoices(choiceValues, correctIndexes);
            if (error == null) {
                long correctCount = answers.stream().filter(Answer::isCorrect).count();
                if (answers.size() < 2) error = "Choice questions need at least 2 choices.";
                else if (MultipleChoiceQuestion.TYPE.equals(type) && correctCount != 1) {
                    error = "Mark exactly one choice as correct.";
                } else if (MultiSelectQuestion.TYPE.equals(type) && correctCount == 0) {
                    error = "Mark at least one choice as correct.";
                }
            }
        } else {
            answers = parseTextAnswers(answerValues);
            if (error == null && answers.isEmpty()) {
                error = "At least one acceptable answer is required.";
            } else if (error == null && MultiAnswerQuestion.TYPE.equals(type) && answers.size() < 2) {
                error = "Multi-answer questions need at least 2 expected answers.";
            }
        }

        if (error != null) {
            model.addAttribute("quiz", quiz);
            model.addAttribute("type", type);
            model.addAttribute("error", error);
            model.addAttribute("body", trimmedBody);
            model.addAttribute("imageUrl", trimmedImage);
            model.addAttribute("fieldsShown", FIELDS_SHOWN);
            return "add-question";
        }

        Question question = newQuestion(type);
        question.setBody(trimmedBody);
        question.setImageUrl(trimmedImage);
        question.setOrdered(MultiAnswerQuestion.TYPE.equals(type) && "on".equals(ordered));
        for (Answer a : answers) question.addAnswer(a);

        quizService.addQuestion(quiz, question);
        return "redirect:/quizzes/edit?id=" + quiz.getId();
    }

    // ===================================================================
    // Summary
    // ===================================================================

    @GetMapping("/view")
    public String view(@RequestParam int id, HttpSession session, Model model) {
        Quiz quiz = quizService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = SessionKeys.currentUser(session);

        model.addAttribute("quiz", quiz);
        model.addAttribute("isOwner", user.getId().equals(quiz.getCreatorId()));
        model.addAttribute("myAttempts", historyService.listForUserOnQuiz(user.getId(), id));
        model.addAttribute("topAllTime",
                privacyService.historyActivities(user, historyService.topAllTime(id, LEADERBOARD_LIMIT)));
        model.addAttribute("topLastDay",
                privacyService.historyActivities(user, historyService.topInLastDay(id, LEADERBOARD_LIMIT)));
        model.addAttribute("recentAttempts",
                privacyService.historyActivities(user, historyService.recentForQuiz(id, RECENT_ATTEMPTS_LIMIT)));
        model.addAttribute("quizStats", historyService.statsForQuiz(id));
        model.addAttribute("attemptCount", historyService.countAttemptsForQuiz(id));
        model.addAttribute("favorite", quizService.isFavorite(id, user.getId()));
        model.addAttribute("favoriteCount", quizService.favoriteCount(id));
        model.addAttribute("ratings", quizService.ratingsFor(id));
        model.addAttribute("averageRating", quizService.averageRating(id));
        return "quiz-summary";
    }

    @PostMapping("/favorite")
    public String favorite(@RequestParam int id,
                           @RequestParam(defaultValue = "true") boolean saved,
                           HttpSession session) {
        User user = SessionKeys.currentUser(session);
        quizService.setFavorite(id, user.getId(), saved);
        return "redirect:/quizzes/view?id=" + id;
    }

    @PostMapping("/rating")
    public String rating(@RequestParam int id,
                         @RequestParam int rating,
                         @RequestParam(required = false) String review,
                         HttpSession session) {
        User user = SessionKeys.currentUser(session);
        quizService.rateQuiz(id, user.getId(), rating, review);
        return "redirect:/quizzes/view?id=" + id;
    }

    @PostMapping("/report")
    public String report(@RequestParam int id,
                         @RequestParam String reason,
                         HttpSession session) {
        User user = SessionKeys.currentUser(session);
        quizService.reportQuiz(id, user.getId(), reason);
        return "redirect:/quizzes/view?id=" + id;
    }

    // ===================================================================
    // Take
    // ===================================================================

    @GetMapping("/take")
    public String startAttempt(@RequestParam int id,
                               @RequestParam(required = false) String practice,
                               HttpSession session,
                               Model model) {
        Quiz quiz = quizService.findByIdFullyLoaded(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (quiz.getQuestions().isEmpty()) {
            return "redirect:/quizzes/view?id=" + id;
        }
        boolean practiceMode = "1".equals(practice) && quiz.isPracticeEnabled();
        QuizAttempt attempt = new QuizAttempt(quiz, quiz.isRandomQuestions(), practiceMode);
        session.setAttribute(SessionKeys.ATTEMPT_PREFIX + id, attempt);
        session.removeAttribute(SessionKeys.COMPLETED_PREFIX + id);
        return renderTakePage(attempt, model);
    }

    @PostMapping("/take")
    public String submitAttempt(@RequestParam int id,
                                @RequestParam(required = false) String intent,
                                HttpServletRequest request,
                                HttpSession session,
                                Model model) {
        QuizAttempt attempt = (QuizAttempt) session.getAttribute(SessionKeys.ATTEMPT_PREFIX + id);
        if (attempt == null) {
            return "redirect:/quizzes/view?id=" + id;
        }
        if (attempt.isPractice() || attempt.getQuiz().isMultiPage()) {
            return handleMultiPageSubmit(attempt, id, intent, request, session, model);
        }
        return handleSinglePageSubmit(attempt, id, request, session);
    }

    private String handleSinglePageSubmit(QuizAttempt attempt, int quizId,
                                          HttpServletRequest request, HttpSession session) {
        for (Question q : attempt.getOrderedQuestions()) {
            String[] response = paramValues(request, "q_" + q.getId());
            attempt.recordResponse(q.getId(), response, q.grade(response));
        }
        return finish(attempt, quizId, session);
    }

    private String handleMultiPageSubmit(QuizAttempt attempt, int quizId, String intent,
                                         HttpServletRequest request, HttpSession session, Model model) {
        if ("advance".equals(intent)) {
            attempt.setLastCorrect(null);
            if (!attempt.isPractice()) {
                attempt.advance();
            }
            if (attempt.isFinished()) return finish(attempt, quizId, session);
            return renderTakePage(attempt, model);
        }

        Question q = attempt.currentQuestion();
        if (q == null) return finish(attempt, quizId, session);

        String[] response = paramValues(request, "q_" + q.getId());
        int score = q.grade(response);
        attempt.recordResponse(q.getId(), response, score);
        if (attempt.isPractice()) {
            attempt.recordPracticeResult(q, score);
        }

        if (attempt.getQuiz().isImmediateCorrection()) {
            attempt.setLastCorrect(score == q.getMaxScore());
            model.addAttribute("quiz", attempt.getQuiz());
            model.addAttribute("attempt", attempt);
            model.addAttribute("question", q);
            model.addAttribute("userAnswerText", AnswerFormatter.formatUserAnswer(q, response));
            model.addAttribute("correctAnswerText", AnswerFormatter.formatCorrectAnswer(q));
            return "quiz-correction";
        }

        if (!attempt.isPractice()) {
            attempt.advance();
        }
        if (attempt.isFinished()) return finish(attempt, quizId, session);
        return renderTakePage(attempt, model);
    }

    private String renderTakePage(QuizAttempt attempt, Model model) {
        Quiz quiz = attempt.getQuiz();
        List<Question> toRender = attempt.isPractice() || quiz.isMultiPage()
                ? Collections.singletonList(attempt.currentQuestion())
                : attempt.getOrderedQuestions();
        model.addAttribute("quiz", quiz);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questionsToRender", toRender);
        return "take-quiz";
    }

    private String finish(QuizAttempt attempt, int quizId, HttpSession session) {
        attempt.markFinished();
        session.setAttribute(SessionKeys.COMPLETED_PREFIX + quizId, attempt);
        session.removeAttribute(SessionKeys.ATTEMPT_PREFIX + quizId);
        return "redirect:/quizzes/result?id=" + quizId;
    }

    // ===================================================================
    // Result
    // ===================================================================

    @GetMapping("/result")
    public String result(@RequestParam int id, HttpSession session, Model model) {
        String key = SessionKeys.COMPLETED_PREFIX + id;
        QuizAttempt attempt = (QuizAttempt) session.getAttribute(key);
        if (attempt == null) {
            return "redirect:/quizzes/view?id=" + id;
        }

        // First render only: persist to history (non-practice) and check achievements.
        if (session.getAttribute(key + ":processed") == null) {
            User user = SessionKeys.currentUser(session);
            if (!attempt.isPractice()) {
                historyService.record(user.getId(), id,
                        attempt.getTotalScore(), attempt.getMaxScore(), attempt.getElapsedSeconds());
            }
            achievementEngine.onQuizCompleted(user.getId(), id, attempt.isPractice());
            session.setAttribute(key + ":processed", Boolean.TRUE);
        }

        Map<Integer, String> userAnswers = new HashMap<>();
        Map<Integer, String> correctAnswers = new HashMap<>();
        for (Question q : attempt.getOrderedQuestions()) {
            String[] resp = attempt.getResponses().get(q.getId());
            userAnswers.put(q.getId(), AnswerFormatter.formatUserAnswer(q, resp));
            correctAnswers.put(q.getId(), AnswerFormatter.formatCorrectAnswer(q));
        }
        int secs = attempt.getElapsedSeconds();
        model.addAttribute("attempt", attempt);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("elapsedMinutes", secs / 60);
        model.addAttribute("elapsedRemainderSeconds", secs % 60);
        return "quiz-result";
    }

    // ===================================================================
    // helpers
    // ===================================================================

    private Quiz loadOwnedQuiz(int quizId, HttpSession session) {
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = SessionKeys.currentUser(session);
        if (!user.getId().equals(quiz.getCreatorId()) && !user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return quiz;
    }

    private void requireValidType(String type) {
        if (!VALID_TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown question type");
        }
    }

    private Question newQuestion(String type) {
        return switch (type) {
            case QuestionResponseQuestion.TYPE -> new QuestionResponseQuestion();
            case FillBlankQuestion.TYPE -> new FillBlankQuestion();
            case PictureResponseQuestion.TYPE -> new PictureResponseQuestion();
            case MultipleChoiceQuestion.TYPE -> new MultipleChoiceQuestion();
            case MultiAnswerQuestion.TYPE -> new MultiAnswerQuestion();
            case MultiSelectQuestion.TYPE -> new MultiSelectQuestion();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown question type");
        };
    }

    private boolean isChoiceType(String type) {
        return MultipleChoiceQuestion.TYPE.equals(type) || MultiSelectQuestion.TYPE.equals(type);
    }

    private List<Answer> parseTextAnswers(List<String> values) {
        List<Answer> out = new ArrayList<>();
        if (values == null) return out;
        int pos = 0;
        for (String v : values) {
            String t = v == null ? null : v.trim();
            if (t != null && !t.isEmpty()) {
                out.add(new Answer(t, true, pos++));
            }
        }
        return out;
    }

    private List<Answer> parseChoices(List<String> choiceTexts, List<Integer> correctIndexes) {
        List<Answer> out = new ArrayList<>();
        if (choiceTexts == null) return out;
        Set<Integer> correct = correctIndexes == null ? Set.of() : Set.copyOf(correctIndexes);
        int pos = 0;
        for (int i = 0; i < choiceTexts.size(); i++) {
            String t = choiceTexts.get(i) == null ? null : choiceTexts.get(i).trim();
            if (t == null || t.isEmpty()) continue;
            out.add(new Answer(t, correct.contains(i), pos++));
        }
        return out;
    }

    private static String[] paramValues(HttpServletRequest req, String name) {
        String[] vs = req.getParameterValues(name);
        return vs == null ? new String[0] : vs;
    }
}
