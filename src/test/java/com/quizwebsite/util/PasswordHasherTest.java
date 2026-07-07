package com.quizwebsite.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void newSaltReturnsHexSalt() {
        String salt = PasswordHasher.newSalt();

        assertEquals(32, salt.length());
        assertTrue(salt.matches("[0-9a-f]+"));
    }

    @Test
    void hashIsDeterministicForSamePasswordAndSalt() {
        String salt = "00112233445566778899aabbccddeeff";

        assertEquals(
                PasswordHasher.hash("secret", salt),
                PasswordHasher.hash("secret", salt));
    }

    @Test
    void verifyAcceptsCorrectPasswordAndRejectsWrongPassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("secret", salt);

        assertTrue(PasswordHasher.verify("secret", salt, hash));
        assertFalse(PasswordHasher.verify("wrong", salt, hash));
    }
}
