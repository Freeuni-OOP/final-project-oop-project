package com.quizwebsite.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Password hashing using BCrypt (it embeds its own salt, so the salt param below isn't used). */
public final class PasswordHasher {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private PasswordHasher() {}

    public static String newSalt() {
        return "";
    }

    public static String hash(String password, String salt) {
        return BCRYPT.encode(password);
    }

    public static boolean verify(String password, String salt, String expectedHash) {
        return expectedHash != null && BCRYPT.matches(password, expectedHash);
    }
}
