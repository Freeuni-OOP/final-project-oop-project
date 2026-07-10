package com.quizwebsite.config;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.model.achievement.AchievementKind;
import com.quizwebsite.model.question.Answer;
import com.quizwebsite.model.question.MultiAnswerQuestion;
import com.quizwebsite.model.question.MultiSelectQuestion;
import com.quizwebsite.model.question.MultipleChoiceQuestion;
import com.quizwebsite.model.question.Question;
import com.quizwebsite.model.question.QuestionResponseQuestion;
import com.quizwebsite.repository.UserRepository;
import com.quizwebsite.service.AchievementService;
import com.quizwebsite.service.AnnouncementService;
import com.quizwebsite.service.FriendshipService;
import com.quizwebsite.service.HistoryService;
import com.quizwebsite.service.QuizService;
import com.quizwebsite.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates a few demo accounts on first startup so there's something to log in with. */
@Component
public class DataInitializer implements ApplicationRunner {

    private final boolean seedEnabled;
    private final UserRepository userRepository;
    private final UserService userService;
    private final QuizService quizService;
    private final FriendshipService friendshipService;
    private final HistoryService historyService;
    private final AnnouncementService announcementService;
    private final AchievementService achievementService;

    public DataInitializer(@Value("${quizwebsite.seed.enabled:true}") boolean seedEnabled,
                           UserRepository userRepository,
                           UserService userService,
                           QuizService quizService,
                           FriendshipService friendshipService,
                           HistoryService historyService,
                           AnnouncementService announcementService,
                           AchievementService achievementService) {
        this.seedEnabled = seedEnabled;
        this.userRepository = userRepository;
        this.userService = userService;
        this.quizService = quizService;
        this.friendshipService = friendshipService;
        this.historyService = historyService;
        this.announcementService = announcementService;
        this.achievementService = achievementService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled || userRepository.count() > 0) return;

        User admin = userService.create("admin", "admin123");
        userService.promoteToAdmin(admin.getId());

        User alex = userService.create("alex", "password");
        alex.setDisplayName("Alex Rivera");
        alex.setBio("Enjoys history and quick quizzes.");

        User sam = userService.create("sam", "password");
        sam.setDisplayName("Sam Chen");
        sam.setBio("Practice mode enthusiast.");

        announcementService.create(admin.getId(), "Welcome to the Spring Quiz Website demo.");

        Quiz history = quiz(admin, "History Starter", "A small history quiz.", true, true, true, true);
        history = quizService.create(history, "History", "demo, history");
        Question q1 = new QuestionResponseQuestion();
        q1.setBody("Who was President during the Bay of Pigs fiasco?");
        q1.addAnswer(new Answer("John F. Kennedy", true, 0));
        q1.addAnswer(new Answer("JFK", true, 1));
        quizService.addQuestion(history, q1);

        Question q2 = new MultiAnswerQuestion();
        q2.setBody("Name three large U.S. cities.");
        q2.setAnswerSlots(3);
        q2.addAnswer(new Answer("New York", true, 0));
        q2.addAnswer(new Answer("Los Angeles", true, 1));
        q2.addAnswer(new Answer("Chicago", true, 2));
        q2.addAnswer(new Answer("Houston", true, 3));
        quizService.addQuestion(history, q2);

        Quiz java = quiz(alex, "Java Basics", "Keywords and basic facts.", false, false, false, true);
        java = quizService.create(java, "Programming", "java, oop");
        Question q3 = new MultipleChoiceQuestion();
        q3.setBody("Which keyword declares a class?");
        q3.addAnswer(new Answer("method", false, 0));
        q3.addAnswer(new Answer("class", true, 1));
        q3.addAnswer(new Answer("package", false, 2));
        quizService.addQuestion(java, q3);

        Question q4 = new MultiSelectQuestion();
        q4.setBody("Select Java access modifiers.");
        q4.addAnswer(new Answer("public", true, 0));
        q4.addAnswer(new Answer("private", true, 1));
        q4.addAnswer(new Answer("banana", false, 2));
        quizService.addQuestion(java, q4);

        friendshipService.request(alex.getId(), sam.getId());
        friendshipService.accept(alex.getId(), sam.getId());

        historyService.record(sam.getId(), history.getId(), 2, history.getMaxScore(), 95);
        historyService.record(alex.getId(), history.getId(), 1, history.getMaxScore(), 120);
        historyService.record(sam.getId(), java.getId(), 2, java.getMaxScore(), 60);

        quizService.setFavorite(history.getId(), alex.getId(), true);
        quizService.setFavorite(java.getId(), sam.getId(), true);
        quizService.rateQuiz(history.getId(), alex.getId(), 5, "Great sample quiz.");
        quizService.rateQuiz(java.getId(), sam.getId(), 4, "Useful Java warmup.");

        achievementService.awardIfMissing(admin.getId(), AchievementKind.AMATEUR_AUTHOR);
        achievementService.awardIfMissing(alex.getId(), AchievementKind.AMATEUR_AUTHOR);
        achievementService.awardIfMissing(sam.getId(), AchievementKind.PRACTICE_MAKES_PERFECT);
    }

    private Quiz quiz(User creator, String name, String description,
                      boolean random, boolean multiPage, boolean immediateCorrection, boolean practice) {
        Quiz quiz = new Quiz();
        quiz.setCreator(creator);
        quiz.setName(name);
        quiz.setDescription(description);
        quiz.setRandomQuestions(random);
        quiz.setMultiPage(multiPage);
        quiz.setImmediateCorrection(immediateCorrection);
        quiz.setPracticeEnabled(practice);
        return quiz;
    }
}
