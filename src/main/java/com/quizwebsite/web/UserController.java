package com.quizwebsite.web;

import com.quizwebsite.model.PrivacySetting;
import com.quizwebsite.model.User;
import com.quizwebsite.service.AchievementService;
import com.quizwebsite.service.FriendshipService;
import com.quizwebsite.service.HistoryService;
import com.quizwebsite.service.PrivacyService;
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

import java.util.Collections;
import java.util.List;

/** Public user pages: lookup by username and the per-user profile page. */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final int RESULT_LIMIT = 50;
    private static final int HISTORY_LIMIT = 20;

    private final UserService userService;
    private final QuizService quizService;
    private final FriendshipService friendshipService;
    private final HistoryService historyService;
    private final AchievementService achievementService;
    private final PrivacyService privacyService;

    public UserController(UserService userService,
                          QuizService quizService,
                          FriendshipService friendshipService,
                          HistoryService historyService,
                          AchievementService achievementService,
                          PrivacyService privacyService) {
        this.userService = userService;
        this.quizService = quizService;
        this.friendshipService = friendshipService;
        this.historyService = historyService;
        this.achievementService = achievementService;
        this.privacyService = privacyService;
    }

    @GetMapping("/lookup")
    public String lookup(@RequestParam(required = false) String q, Model model) {
        List<User> results;
        if (q == null || q.trim().isEmpty()) {
            results = Collections.emptyList();
        } else {
            results = userService.searchByUsername(q.trim(), RESULT_LIMIT);
        }
        model.addAttribute("query", q);
        model.addAttribute("results", results);
        return "user-lookup";
    }

    @GetMapping("/view")
    public String view(@RequestParam int id, HttpSession session, Model model) {
        User profile = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        User viewer = SessionKeys.currentUser(session);

        model.addAttribute("profile", profile);
        model.addAttribute("isSelf", viewer.getId().equals(profile.getId()));
        model.addAttribute("friendStatus", computeFriendStatus(viewer, profile));
        model.addAttribute("privacySettings", PrivacySetting.values());
        model.addAttribute("theirQuizzes", quizService.listByCreator(id));
        boolean canViewHistory = privacyService.canViewHistory(viewer, profile);
        model.addAttribute("canViewHistory", canViewHistory);
        model.addAttribute("theirHistory", canViewHistory ? historyService.listForUser(id, HISTORY_LIMIT) : List.of());
        model.addAttribute("theirAchievements", achievementService.listForUser(id));
        return "user";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(required = false) String displayName,
                                @RequestParam(required = false) String bio,
                                @RequestParam(defaultValue = "PUBLIC") PrivacySetting privacySetting,
                                HttpSession session) {
        User viewer = SessionKeys.currentUser(session);
        User updated = userService.updateProfile(viewer.getId(), displayName, bio, privacySetting);
        session.setAttribute(SessionKeys.USER, updated);
        return "redirect:/users/view?id=" + updated.getId();
    }

    /** SELF, FRIENDS, REQUEST_SENT, REQUEST_RECEIVED, or NONE — drives the profile buttons. */
    private String computeFriendStatus(User viewer, User profile) {
        if (viewer.getId().equals(profile.getId())) return "SELF";
        if (friendshipService.areFriends(viewer.getId(), profile.getId())) return "FRIENDS";
        if (friendshipService.pendingFromTo(viewer.getId(), profile.getId())) return "REQUEST_SENT";
        if (friendshipService.pendingFromTo(profile.getId(), viewer.getId())) return "REQUEST_RECEIVED";
        return "NONE";
    }
}
