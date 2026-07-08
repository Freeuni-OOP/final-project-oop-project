package com.quizwebsite.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** BCrypt password hashing with legacy salted SHA-256 verification support. */
public final class PasswordHasher {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private static final String BCRYPT_MARKER = "bcrypt";

    private PasswordHasher() {}

    public static String newSalt() {
        return BCRYPT_MARKER;
    }

    public static String hash(String password, String saltHex) {
        return BCRYPT.encode(password);
    }

    public static boolean verify(String password, String saltHex, String expectedHashHex) {
        if (expectedHashHex == null || expectedHashHex.isBlank()) return false;
        if (isBcrypt(expectedHashHex)) {
            return BCRYPT.matches(password, expectedHashHex);
        }
        String actual = legacySha256(password, saltHex);
        return constantTimeEquals(actual, expectedHashHex);
    }

    private static boolean isBcrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    private static String legacySha256(String password, String saltHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((saltHex == null ? "" : saltHex).getBytes(StandardCharsets.UTF_8));
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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
