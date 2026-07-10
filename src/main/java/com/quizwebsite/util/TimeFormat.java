package com.quizwebsite.util;

/** Formats durations like "5m 32s" for the templates. */
public final class TimeFormat {

    private TimeFormat() {}

    public static String mmss(int totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        if (m == 0) return s + "s";
        return m + "m " + s + "s";
    }
}
