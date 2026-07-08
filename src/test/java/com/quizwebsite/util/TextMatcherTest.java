package com.quizwebsite.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextMatcherTest {

    @Test
    void normalizeTrimsLowercasesAndCollapsesWhitespace() {
        assertEquals("george washington", TextMatcher.normalize("  George   Washington  "));
    }

    @Test
    void matchesAnyAcceptsNormalizedAnswer() {
        assertTrue(TextMatcher.matchesAny("  jFk ", List.of("John F. Kennedy", "JFK")));
        assertFalse(TextMatcher.matchesAny("Nixon", List.of("John F. Kennedy", "JFK")));
    }
}
