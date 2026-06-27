package com.quizwebsite.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** The logged-in landing page. */
@Controller
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
