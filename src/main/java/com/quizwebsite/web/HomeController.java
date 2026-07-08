package com.quizwebsite.web;

import com.quizwebsite.model.User;
import com.quizwebsite.service.AchievementService;
import com.quizwebsite.service.AnnouncementService;
import com.quizwebsite.service.FriendshipService;
import com.quizwebsite.service.HistoryService;
import com.quizwebsite.service.MessageService;
import com.quizwebsite.service.PrivacyService;
import com.quizwebsite.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** The logged-in home page (also the welcome target of "/"). */
@Controller
public class HomeController {

    private final QuizService quizService;
    private final MessageService messageService;
    private final HistoryService historyService;
    private final AnnouncementService announcementService;
    private final AchievementService achievementService;
    private final FriendshipService friendshipService;
    private final PrivacyService privacyService;

    public HomeController(QuizService quizService,
                          MessageService messageService,
                          HistoryService historyService,
                          AnnouncementService announcementService,
                          AchievementService achievementService,
                          FriendshipService friendshipService,
                          PrivacyService privacyService) {
        this.quizService = quizService;
        this.messageService = messageService;
        this.historyService = historyService;
        this.announcementService = announcementService;
        this.achievementService = achievementService;
        this.friendshipService = friendshipService;
        this.privacyService = privacyService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = SessionKeys.currentUser(session);
        model.addAttribute("announcements", announcementService.listRecent(5));
        model.addAttribute("popularQuizzes", quizService.listPopular(5));
        model.addAttribute("recentQuizzes", quizService.listRecent(5));
        model.addAttribute("savedQuizzes", quizService.savedBy(user.getId(), 5));
        model.addAttribute("recentMessages", messageService.inbox(user.getId(), 5));
        model.addAttribute("myRecentAttempts", historyService.listForUser(user.getId(), 5));
        model.addAttribute("myRecentQuizzes", quizService.listByCreator(user.getId()));
        model.addAttribute("myAchievements", achievementService.listForUser(user.getId()));
        var friendIds = friendshipService.listFriendIds(user.getId());
        model.addAttribute("friendRecentAttempts",
                privacyService.visibleHistoryActivities(user, historyService.listForUsers(friendIds, 5)));
        model.addAttribute("friendRecentQuizzes", quizService.listByCreators(friendIds, 5));
        model.addAttribute("friendRecentAchievements", achievementService.recentForUsers(friendIds, 5));
        return "home";
    }
}
