package com.quizwebsite.config;

import com.quizwebsite.model.User;
import com.quizwebsite.repository.UserRepository;
import com.quizwebsite.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates a few demo accounts on first startup so there's something to log in with. */
@Component
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User admin = userService.create("admin", "admin123");
        userService.promoteToAdmin(admin.getId());

        User alex = userService.create("alex", "password");
        alex.setDisplayName("Alex Rivera");
        alex.setBio("Enjoys history and quick quizzes.");

        User sam = userService.create("sam", "password");
        sam.setDisplayName("Sam Chen");
        sam.setBio("Practice mode enthusiast.");
    }
}
