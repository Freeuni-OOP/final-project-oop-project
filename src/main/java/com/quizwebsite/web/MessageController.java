package com.quizwebsite.web;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.model.social.Message;
import com.quizwebsite.service.FriendshipService;
import com.quizwebsite.service.MessageService;
import com.quizwebsite.service.QuizService;
import com.quizwebsite.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** The internal mail system: inbox, notes, and quiz challenges. */
@Controller
public class MessageController {

    private static final int INBOX_LIMIT = 100;

    private final MessageService messageService;
    private final UserService userService;
    private final QuizService quizService;
    private final FriendshipService friendshipService;

    public MessageController(MessageService messageService,
                             UserService userService,
                             QuizService quizService,
                             FriendshipService friendshipService) {
        this.messageService = messageService;
        this.userService = userService;
        this.quizService = quizService;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/messages")
    public String inbox(HttpSession session, Model model) {
        User me = SessionKeys.currentUser(session);
        List<Message> inbox = messageService.inbox(me.getId(), INBOX_LIMIT);
        // Mark everything except friend-requests read (their accept/decline buttons stay obvious).
        for (Message m : inbox) {
            if (!m.isRead() && !Message.TYPE_FRIEND_REQUEST.equals(m.getType())) {
                messageService.markRead(m.getId());
                m.setRead(true);
            }
        }
        model.addAttribute("inbox", inbox);
        return "inbox";
    }

    @PostMapping("/messages")
    public String delete(@RequestParam String intent,
                         @RequestParam int messageId,
                         HttpSession session) {
        if (!"delete".equals(intent)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User me = SessionKeys.currentUser(session);
        messageService.findById(messageId)
                .filter(m -> me.getId().equals(m.getToUserId()))
                .ifPresent(m -> messageService.delete(messageId));
        return "redirect:/messages";
    }

    // ---- notes ----

    @GetMapping("/messages/send")
    public String noteForm(@RequestParam("to") int toId, Model model) {
        User to = userService.findById(toId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("recipient", to);
        return "send-note";
    }

    @PostMapping("/messages/send")
    public String sendNote(@RequestParam("to") int toId,
                           @RequestParam String body,
                           HttpSession session) {
        User me = SessionKeys.currentUser(session);
        if (body == null || body.trim().isEmpty() || toId == me.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        messageService.sendNote(me.getId(), toId, body.trim());
        return "redirect:/users/view?id=" + toId;
    }

    // ---- challenges ----

    @GetMapping("/messages/challenge")
    public String challengeForm(@RequestParam int quizId, HttpSession session, Model model) {
        User me = SessionKeys.currentUser(session);
        Quiz quiz = quizService.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("quiz", quiz);
        model.addAttribute("friends", friendshipService.listFriends(me.getId()));
        return "send-challenge";
    }

    @PostMapping("/messages/challenge")
    public String sendChallenge(@RequestParam int quizId,
                                @RequestParam("to") int toId,
                                HttpSession session) {
        User me = SessionKeys.currentUser(session);
        if (toId == me.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (!friendshipService.areFriends(me.getId(), toId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        messageService.sendChallenge(me.getId(), toId, quizId);
        return "redirect:/quizzes/view?id=" + quizId;
    }
}
