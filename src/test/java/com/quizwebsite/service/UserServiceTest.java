package com.quizwebsite.service;

import com.quizwebsite.model.User;
import com.quizwebsite.repository.UserRepository;
import com.quizwebsite.util.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createStoresSaltedHashInsteadOfRawPassword() {
        User user = userService.create("hash_user", "plain-password");

        assertTrue(userRepository.existsByUsername("hash_user"));
        assertNotEquals("plain-password", user.getPasswordHash());
        assertTrue(PasswordHasher.verify("plain-password", user.getSalt(), user.getPasswordHash()));
    }

    @Test
    void checkPasswordUsesStoredSaltAndHash() {
        User user = userService.create("login_user", "correct-password");

        assertTrue(userService.checkPassword(user, "correct-password"));
        assertFalse(userService.checkPassword(user, "wrong-password"));
    }
}
