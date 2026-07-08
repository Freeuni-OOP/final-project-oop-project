package com.quizwebsite.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void newSaltReturnsEmptyString() {
        String salt = PasswordHasher.newSalt();

        assertEquals("", salt);
    }

    @Test
    void hashUsesBcryptAndIsSalted() {
        String salt = "00112233445566778899aabbccddeeff";

        String first = PasswordHasher.hash("secret", salt);
        String second = PasswordHasher.hash("secret", salt);

        assertTrue(first.startsWith("$2"));
        assertTrue(second.startsWith("$2"));
        assertFalse(first.equals(second));
    }

    @Test
    void verifyAcceptsCorrectPasswordAndRejectsWrongPassword() {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash("secret", salt);

        assertTrue(PasswordHasher.verify("secret", salt, hash));
        assertFalse(PasswordHasher.verify("wrong", salt, hash));
    }
}
