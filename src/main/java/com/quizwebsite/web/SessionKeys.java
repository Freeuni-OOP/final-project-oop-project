package com.quizwebsite.web;

import com.quizwebsite.model.User;
import jakarta.servlet.http.HttpSession;

/** definitions of the session attribute keys used across the app. */
public final class SessionKeys {

    /** The logged-in {@link User}. */
    public static final String USER = "user";

    private SessionKeys() {}

    /** Convenience: the current user, or null if not logged in. */
    public static User currentUser(HttpSession session) {
        return session == null ? null : (User) session.getAttribute(USER);
    }
}
