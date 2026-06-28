package com.quizwebsite.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** The logged-in home page */
@Controller
public class HomeController {

    /* redirect to home page. User not logged in is handled by the auth interceptor */
    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
