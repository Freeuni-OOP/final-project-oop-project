package com.quizwebsite.web;

import com.quizwebsite.model.User;
import com.quizwebsite.service.FriendshipService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/** Friends list + the POST-only friend-action dispatch (request/accept/decline/remove). */
@Controller
public class FriendController {

    private final FriendshipService friendshipService;

    public FriendController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/friends")
    public String friends(HttpSession session, Model model) {
        User me = SessionKeys.currentUser(session);
        model.addAttribute("friends", friendshipService.listFriends(me.getId()));
        return "friends";
    }

    @PostMapping("/friends/action")
    public String action(@RequestParam int otherId,
                         @RequestParam String intent,
                         @RequestParam(required = false) String returnTo,
                         HttpSession session) {
        User me = SessionKeys.currentUser(session);
        switch (intent) {
            case "request" -> friendshipService.request(me.getId(), otherId);
            case "accept"  -> friendshipService.accept(otherId, me.getId());
            case "decline" -> friendshipService.decline(otherId, me.getId());
            case "remove"  -> friendshipService.remove(me.getId(), otherId);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown intent");
        }
        if (returnTo == null || returnTo.isEmpty()) {
            return "redirect:/users/view?id=" + otherId;
        }
        return "redirect:" + returnTo;
    }
}
