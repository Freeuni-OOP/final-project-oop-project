package com.quizwebsite.service;

import com.quizwebsite.model.User;
import com.quizwebsite.repository.UserRepository;
import com.quizwebsite.util.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** User accounts: registration with salted hashing and login checks. */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
