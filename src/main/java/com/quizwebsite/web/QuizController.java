package com.quizwebsite.web;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.service.QuizService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/** Browsing and creating quizzes. */
@Controller
@RequestMapping("/quizzes")
public class QuizController {

    private static final int RECENT_LIMIT = 50;

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("quizzes", quizService.listRecent(RECENT_LIMIT));
        return "quizzes";
    }

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

        quizService.create(quiz);
        return "redirect:/quizzes";
    }
}
