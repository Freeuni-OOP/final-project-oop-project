package com.quizwebsite.util;

/**
 * Tiny utility for formatting durations in the JSPs.
 * EL's math support is too limited to do clean "5m 32s" formatting inline.
 */
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
