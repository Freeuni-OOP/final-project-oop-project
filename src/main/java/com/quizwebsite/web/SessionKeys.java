package com.quizwebsite.web;

import com.quizwebsite.model.User;
import jakarta.servlet.http.HttpSession;

/** Central definition of the session attribute keys used across the app. */
public final class SessionKeys {

    /** The logged-in {@link User}. */
    public static final String USER = "user";

    /** In-progress quiz attempt, suffixed with the quiz id: {@code attempt:<id>}. */
    public static final String ATTEMPT_PREFIX = "attempt:";

    /** Finished quiz attempt awaiting its results page: {@code completed:<id>}. */
    public static final String COMPLETED_PREFIX = "completed:";

    private SessionKeys() {}

    /** Convenience: the current user, or null if not logged in. */
    public static User currentUser(HttpSession session) {
        return session == null ? null : (User) session.getAttribute(USER);
    }
}
