package com.quizwebsite.service;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import com.quizwebsite.repository.AchievementRepository;
import com.quizwebsite.repository.FriendshipRepository;
import com.quizwebsite.repository.HistoryRepository;
import com.quizwebsite.repository.MessageRepository;
import com.quizwebsite.repository.UserRepository;
import com.quizwebsite.util.PasswordHasher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** User accounts: registration with salted hashing, lookup, admin actions. */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final QuizService quizService;
    private final HistoryRepository historyRepository;
    private final AchievementRepository achievementRepository;
    private final FriendshipRepository friendshipRepository;
    private final MessageRepository messageRepository;

    public UserService(UserRepository userRepository,
                       QuizService quizService,
                       HistoryRepository historyRepository,
                       AchievementRepository achievementRepository,
                       FriendshipRepository friendshipRepository,
                       MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.quizService = quizService;
        this.historyRepository = historyRepository;
        this.achievementRepository = achievementRepository;
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean usernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    /** Creates a user with a fresh salt and salted hash of the password. */
    @Transactional
    public User create(String username, String rawPassword) {
        String salt = PasswordHasher.newSalt();
        User user = new User();
        user.setUsername(username);
        user.setSalt(salt);
        user.setPasswordHash(PasswordHasher.hash(rawPassword, salt));
        user.setAdmin(false);
        return userRepository.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return PasswordHasher.verify(rawPassword, user.getSalt(), user.getPasswordHash());
    }

    @Transactional
    public User updateProfile(int userId, String displayName, String bio, com.quizwebsite.model.PrivacySetting privacySetting) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setDisplayName(clean(displayName, 80));
        user.setBio(clean(bio, 1000));
        user.setPrivacySetting(privacySetting == null ? com.quizwebsite.model.PrivacySetting.PUBLIC : privacySetting);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> searchByUsername(String query, int limit) {
        return userRepository.searchByUsername(query, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public long countAll() {
        return userRepository.count();
    }

    @Transactional
    public void promoteToAdmin(int userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setAdmin(true);
            userRepository.save(u);
        });
    }

    /**
     * Removes a user and everything that depended on them: the quizzes they authored
     * (with questions / answers / history / challenge messages), their own quiz history,
     * achievements, friendships, and any messages to or from them.
     */
    @Transactional
    public void delete(int userId) {
        for (Quiz quiz : quizService.listByCreator(userId)) {
            quizService.delete(quiz.getId());
        }
        historyRepository.deleteByUserId(userId);
        achievementRepository.deleteByUserId(userId);
        friendshipRepository.deleteAllForUser(userId);
        messageRepository.deleteAllForUser(userId);
        quizService.deleteUserExtensions(userId);
        userRepository.deleteById(userId);
    }

    private String clean(String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) return null;
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
