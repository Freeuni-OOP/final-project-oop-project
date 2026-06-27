package com.quizwebsite.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Salted SHA-256 password hashing — matches the HW4 Cracker style the spec calls out.
 *
 * Stored per user: a random salt (hex) + the hex digest of (salt || password).
 * Verification recomputes the digest and compares constant-time.
 */
public final class PasswordHasher {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int SALT_BYTES = 16;

    private PasswordHasher() {}

    public static String newSalt() {
        byte[] saltBytes = new byte[SALT_BYTES];
        RNG.nextBytes(saltBytes);
        return toHex(saltBytes);
    }

    public static String hash(String password, String saltHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(saltHex.getBytes(StandardCharsets.UTF_8));
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean verify(String password, String saltHex, String expectedHashHex) {
        String actual = hash(password, saltHex);
        return constantTimeEquals(actual, expectedHashHex);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
