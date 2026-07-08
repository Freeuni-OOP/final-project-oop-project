package com.quizwebsite.web;

import com.quizwebsite.model.User;
import com.quizwebsite.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects shared values into every view's model so templates don't each have to
 * fetch them:
 *   currentUser         — the logged-in user (or null)
 *   nav_unreadMessages  — unread message badge count for the header nav
 *
 * Replaces the old NavCountersFilter.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final MessageService messageService;

    public GlobalModelAdvice(MessageService messageService) {
        this.messageService = messageService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        return SessionKeys.currentUser(session);
    }

    @ModelAttribute("nav_unreadMessages")
    public long unreadMessages(HttpSession session) {
        User user = SessionKeys.currentUser(session);
        if (user == null) return 0;
        try {
            return messageService.unreadCount(user.getId());
        } catch (Exception e) {
            return 0; // header just renders without the badge if the DB hiccups
        }
    }
}
