package com.quizwebsite.model.achievement;

/**
 * The six spec-required achievement kinds, each mapped to a display name and a
 * "how earned" description (used as the tooltip on the achievement badge).
 *
 * The kind's {@link #name()} matches the {@code achievements.kind} string stored
 * in the DB, so adding a new achievement = add an enum constant + a rule in
 * {@link com.quizwebsite.service.AchievementEngine}.
 */
public enum AchievementKind {

    AMATEUR_AUTHOR        ("Amateur Author",        "Created a quiz."),
    PROLIFIC_AUTHOR       ("Prolific Author",       "Created five quizzes."),
    PRODIGIOUS_AUTHOR     ("Prodigious Author",     "Created ten quizzes."),
    QUIZ_MACHINE          ("Quiz Machine",          "Took ten quizzes."),
    I_AM_THE_GREATEST     ("I am the Greatest",     "Held the highest score on a quiz."),
    PRACTICE_MAKES_PERFECT("Practice Makes Perfect","Took a quiz in practice mode.");

    private final String displayName;
    private final String description;

    AchievementKind(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /** Looks up a kind by its string key, returning null if unknown (e.g., legacy data). */
    public static AchievementKind fromKey(String key) {
        if (key == null) return null;
        try { return AchievementKind.valueOf(key); } catch (IllegalArgumentException e) { return null; }
    }
}
