package com.quizwebsite.web;

import com.quizwebsite.model.Answer;
import com.quizwebsite.model.MultipleChoiceQuestion;
import com.quizwebsite.model.Question;
import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

/** Browsing, creating and editing quizzes. */
@Controller
@RequestMapping("/quizzes")
public class QuizController {

    private static final int RECENT_LIMIT = 50;
    private static final int CHOICE_FIELDS = 5;

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // ---------- browsing ----------

    @GetMapping
    public String list(Model model) {
        model.addAttribute("quizzes", quizService.listRecent(RECENT_LIMIT));
        return "quizzes";
    }

    // ---------- creating a quiz ----------

    @GetMapping("/new")
    public String createForm() {
        return "create-quiz";
    }

    @PostMapping("/new")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String description,
                         HttpSession session,
                         Model model) {
        User user = SessionKeys.currentUser(session);
        String trimmedName = name == null ? null : name.trim();
        if (trimmedName == null || trimmedName.isEmpty()) {
            model.addAttribute("error", "Quiz name is required.");
            model.addAttribute("description", description);
            return "create-quiz";
        }

        Quiz quiz = new Quiz();
        quiz.setCreator(user);
        quiz.setName(trimmedName);
        quiz.setDescription(description == null ? null : description.trim());

        Quiz saved = quizService.create(quiz);
        return "redirect:/quizzes/edit?id=" + saved.getId();
    }

    // ---------- editing / adding questions ----------

    @GetMapping("/edit")
    public String edit(@RequestParam int id, HttpSession session, Model model) {
        Quiz quiz = loadOwnedQuiz(id, session);
        model.addAttribute("quiz", quiz);
        return "edit-quiz";
    }

    @GetMapping("/add-question")
    public String addQuestionForm(@RequestParam int quizId, HttpSession session, Model model) {
        Quiz quiz = loadOwnedQuiz(quizId, session);
        model.addAttribute("quiz", quiz);
        model.addAttribute("choiceFields", CHOICE_FIELDS);
        return "add-question";
    }

    @PostMapping("/add-question")
    public String addQuestion(@RequestParam int quizId,
                              @RequestParam(required = false) String body,
                              @RequestParam(name = "choice", required = false) List<String> choiceValues,
                              @RequestParam(name = "correct", required = false) Integer correctIndex,
                              HttpSession session,
                              Model model) {
        Quiz quiz = loadOwnedQuiz(quizId, session);

        String trimmedBody = body == null ? null : body.trim();
        List<Answer> choices = parseChoices(choiceValues, correctIndex);
        long correctCount = choices.stream().filter(Answer::isCorrect).count();

        String error = null;
        if (trimmedBody == null || trimmedBody.isEmpty()) error = "Question text is required.";
        else if (choices.size() < 2) error = "Multiple-choice questions need at least 2 choices.";
        else if (correctCount != 1) error = "Mark exactly one choice as correct.";

        if (error != null) {
            model.addAttribute("quiz", quiz);
            model.addAttribute("error", error);
            model.addAttribute("body", trimmedBody);
            model.addAttribute("choiceFields", CHOICE_FIELDS);
            return "add-question";
        }

        Question question = new MultipleChoiceQuestion();
        question.setBody(trimmedBody);
        for (Answer a : choices) question.addAnswer(a);

        quizService.addQuestion(quiz, question);
        return "redirect:/quizzes/edit?id=" + quiz.getId();
    }

    // ---------- helpers ----------

    /** Loads a quiz and checks that the logged-in user is its creator. */
    private Quiz loadOwnedQuiz(int quizId, HttpSession session) {
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User user = SessionKeys.currentUser(session);
        if (!user.getId().equals(quiz.getCreatorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return quiz;
    }

    private List<Answer> parseChoices(List<String> choiceTexts, Integer correctIndex) {
        List<Answer> out = new ArrayList<>();
        if (choiceTexts == null) return out;
        int correct = correctIndex == null ? -1 : correctIndex;
        int pos = 0;
        for (int i = 0; i < choiceTexts.size(); i++) {
            String t = choiceTexts.get(i) == null ? null : choiceTexts.get(i).trim();
            if (t == null || t.isEmpty()) continue;
            out.add(new Answer(t, i == correct, pos++));
        }
        return out;
    }
}
