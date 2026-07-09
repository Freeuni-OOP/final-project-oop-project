package com.quizwebsite.web;

import com.quizwebsite.model.User;
import com.quizwebsite.model.activity.ReportStatus;
import com.quizwebsite.service.AnnouncementService;
import com.quizwebsite.service.HistoryService;
import com.quizwebsite.service.QuizService;
import com.quizwebsite.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/** Site administration. The AdminInterceptor guards every path here. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final QuizService quizService;
    private final HistoryService historyService;
    private final AnnouncementService announcementService;

    public AdminController(UserService userService,
                           QuizService quizService,
                           HistoryService historyService,
                           AnnouncementService announcementService) {
        this.userService = userService;
        this.quizService = quizService;
        this.historyService = historyService;
        this.announcementService = announcementService;
    }

    // ---- dashboard ----

    @GetMapping
    public String home(Model model) {
        model.addAttribute("userCount", userService.countAll());
        model.addAttribute("quizCount", quizService.countAll());
        model.addAttribute("attemptCount", historyService.countAttemptsAll());
        model.addAttribute("openReportCount", quizService.openReportCount());
        model.addAttribute("openReports", quizService.openReports());
        model.addAttribute("reportStatuses", ReportStatus.values());
        model.addAttribute("recentAnnouncements", announcementService.listRecent(5));
        return "admin/home";
    }

    // ---- users ----

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.listAll());
        return "admin/users";
    }

    @PostMapping("/users")
    public String userAction(@RequestParam String intent,
                             @RequestParam int userId,
                             HttpSession session) {
        User me = SessionKeys.currentUser(session);
        switch (intent) {
            case "promote" -> userService.promoteToAdmin(userId);
            case "remove" -> {
                if (userId == me.getId()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "You can't delete your own account from the admin page.");
                }
                userService.delete(userId);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown intent");
        }
        return "redirect:/admin/users";
    }

    // ---- quizzes ----

    @GetMapping("/quizzes")
    public String quizzes(Model model) {
        model.addAttribute("quizzes", quizService.listRecent(500));
        return "admin/quizzes";
    }

    @PostMapping("/quizzes")
    public String quizAction(@RequestParam String intent,
                             @RequestParam int quizId) {
        switch (intent) {
            case "remove" -> quizService.delete(quizId);
            case "clearHistory" -> historyService.clearForQuiz(quizId);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown intent");
        }
        return "redirect:/admin/quizzes";
    }

    @PostMapping("/reports")
    public String reportAction(@RequestParam int reportId,
                               @RequestParam ReportStatus status) {
        quizService.resolveReport(reportId, status);
        return "redirect:/admin";
    }

    // ---- announcements ----

    @GetMapping("/announcements")
    public String announcements(Model model) {
        model.addAttribute("announcements", announcementService.listRecent(100));
        return "admin/announcements";
    }

    @PostMapping("/announcements")
    public String announcementAction(@RequestParam String intent,
                                     @RequestParam(required = false) String body,
                                     @RequestParam(required = false) Integer id,
                                     HttpSession session) {
        User me = SessionKeys.currentUser(session);
        switch (intent) {
            case "create" -> {
                if (body == null || body.trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body required");
                }
                announcementService.create(me.getId(), body.trim());
            }
            case "delete" -> {
                if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                announcementService.delete(id);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown intent");
        }
        return "redirect:/admin/announcements";
    }
}
