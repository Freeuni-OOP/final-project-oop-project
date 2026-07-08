package com.quizwebsite.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void newSaltReturnsBcryptMarker() {
        String salt = PasswordHasher.newSalt();

        assertEquals("bcrypt", salt);
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

    @Test
    void verifyStillAcceptsLegacySaltedSha256Hash() {
        String salt = "00112233445566778899aabbccddeeff";
        String legacyHash = "a646118b31dc9839381df254dd210eedac8fb69a7207c3946ed58a9d8d0320a0";

        assertTrue(PasswordHasher.verify("secret", salt, legacyHash));
        assertFalse(PasswordHasher.verify("wrong", salt, legacyHash));
    }
}
