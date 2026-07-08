package com.quizwebsite.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable, session-bound state for one in-progress quiz attempt.
 *
 * Lives in HttpSession under the key {@code attempt:&lt;quizId&gt;} while the user
 * is taking the quiz, and is moved to {@code completed:&lt;quizId&gt;} once finished
 * so the results page can render rich per-question feedback.
 *
 * Storing the whole {@link Quiz} (including all questions and answers) in the
 * session is fine — quizzes are small and the spec says we don't need to handle
 * concurrent access to the same data structures.
 */
public class QuizAttempt implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int PRACTICE_CORRECT_TARGET = 3;

    private final Quiz quiz;
    private final List<Question> orderedQuestions;
    private final List<Question> activePracticeQuestions;
    private final long startedAtMillis;
    private final boolean practice;

    /** questionId -&gt; the raw String[] the user submitted (so we can replay on result page). */
    private final Map<Integer, String[]> responses = new HashMap<>();
    /** questionId -&gt; points earned on that question. */
    private final Map<Integer, Integer> scores = new HashMap<>();
    /** questionId -&gt; how many times it has been answered correctly in this practice session. */
    private final Map<Integer, Integer> practiceCorrectCounts = new HashMap<>();

    private int currentIndex = 0;
    private long finishedAtMillis = 0L;

    /** Set after grading a multi-page question with immediate-correction on; cleared on advance. */
    private Boolean lastCorrect;

    public QuizAttempt(Quiz quiz, boolean shuffle, boolean practice) {
        this.quiz = quiz;
        this.orderedQuestions = new ArrayList<>(quiz.getQuestions());
        if (shuffle) Collections.shuffle(this.orderedQuestions);
        this.activePracticeQuestions = new ArrayList<>(this.orderedQuestions);
        this.startedAtMillis = System.currentTimeMillis();
        this.practice = practice;
    }

    public Quiz getQuiz() { return quiz; }
    public boolean isPractice() { return practice; }

    public List<Question> getOrderedQuestions() { return orderedQuestions; }

    public int getTotal() { return orderedQuestions.size(); }
    public int getCurrentIndex() { return currentIndex; }

    public Question currentQuestion() {
        if (practice) {
            if (activePracticeQuestions.isEmpty()) return null;
            if (currentIndex >= activePracticeQuestions.size()) currentIndex = 0;
            return activePracticeQuestions.get(currentIndex);
        }
        if (currentIndex < 0 || currentIndex >= orderedQuestions.size()) return null;
        return orderedQuestions.get(currentIndex);
    }

    public boolean isFinished() {
        return practice ? activePracticeQuestions.isEmpty() : currentIndex >= orderedQuestions.size();
    }

    public void recordResponse(int questionId, String[] response, int score) {
        responses.put(questionId, response);
        scores.put(questionId, score);
    }

    public void advance() {
        if (practice) {
            advancePracticeIndex();
        } else {
            currentIndex++;
        }
    }

    public void recordPracticeResult(Question question, int score) {
        if (!practice || question == null) return;
        if (score == question.getMaxScore()) {
            int correct = getPracticeCorrectCount(question.getId()) + 1;
            practiceCorrectCounts.put(question.getId(), correct);
            if (correct >= PRACTICE_CORRECT_TARGET) {
                activePracticeQuestions.removeIf(q -> Objects.equals(q.getId(), question.getId()));
                if (currentIndex >= activePracticeQuestions.size()) currentIndex = 0;
                return;
            }
        }
        advancePracticeIndex();
    }

    private void advancePracticeIndex() {
        if (activePracticeQuestions.isEmpty()) return;
        currentIndex = (currentIndex + 1) % activePracticeQuestions.size();
    }

    public void markFinished() {
        finishedAtMillis = System.currentTimeMillis();
    }

    public int getTotalScore() {
        int sum = 0;
        for (Integer s : scores.values()) sum += s;
        return sum;
    }

    public int getMaxScore() {
        int sum = 0;
        for (Question q : orderedQuestions) sum += q.getMaxScore();
        return sum;
    }

    public int getElapsedSeconds() {
        long end = finishedAtMillis > 0 ? finishedAtMillis : System.currentTimeMillis();
        return (int) Math.max(0L, (end - startedAtMillis) / 1000L);
    }

    public Map<Integer, String[]> getResponses() { return responses; }
    public Map<Integer, Integer> getScores() { return scores; }

    public Boolean getLastCorrect() { return lastCorrect; }
    public void setLastCorrect(Boolean lastCorrect) { this.lastCorrect = lastCorrect; }

    public Map<Integer, Integer> getPracticeCorrectCounts() { return practiceCorrectCounts; }

    public int getPracticeCorrectCount(Integer questionId) {
        if (questionId == null) return 0;
        return practiceCorrectCounts.getOrDefault(questionId, 0);
    }

    public int getPracticeRemaining() { return activePracticeQuestions.size(); }

    public int getPracticeMastered() { return orderedQuestions.size() - activePracticeQuestions.size(); }

    public int getPracticeCorrectTarget() { return PRACTICE_CORRECT_TARGET; }
}
