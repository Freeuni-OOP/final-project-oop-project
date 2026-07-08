package com.quizwebsite.web;

import com.quizwebsite.model.User;
import com.quizwebsite.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/** Login, registration, and logout. These are the only auth-free pages. */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isEmpty() || !userService.checkPassword(user.get(), password)) {
            model.addAttribute("error", "Invalid username or password.");
            model.addAttribute("username", username);
            return "login";
        }
        session.setAttribute(SessionKeys.USER, user.get());
        return "redirect:/home";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String confirm,
                           HttpSession session,
                           Model model) {
        String error = validate(username, password, confirm);
        if (error == null && userService.usernameTaken(username)) {
            error = "Username is already taken.";
        }
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("username", username);
            return "register";
        }
        User user = userService.create(username, password);
        session.setAttribute(SessionKeys.USER, user);
        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private String validate(String username, String password, String confirm) {
        if (username == null || username.trim().isEmpty()) return "Username is required.";
        if (username.length() > 64) return "Username must be 64 characters or fewer.";
        if (password == null || password.isEmpty()) return "Password is required.";
        if (!password.equals(confirm)) return "Passwords do not match.";
        return null;
    }
}
