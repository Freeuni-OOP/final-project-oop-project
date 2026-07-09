package com.quizwebsite.service;

import com.quizwebsite.model.social.Message;
import com.quizwebsite.repository.HistoryRepository;
import com.quizwebsite.repository.MessageRepository;
import com.quizwebsite.repository.QuizRepository;
import com.quizwebsite.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Internal mail: notes, challenges, and the inbox. */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final HistoryRepository historyRepository;

    public MessageService(MessageRepository messageRepository,
                          UserRepository userRepository,
                          QuizRepository quizRepository,
                          HistoryRepository historyRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void sendNote(int fromId, int toId, String body) {
        Message m = baseMessage(fromId, toId, Message.TYPE_NOTE);
        m.setBody(body);
        messageRepository.save(m);
    }

    @Transactional
    public void sendChallenge(int fromId, int toId, int quizId) {
        Message m = baseMessage(fromId, toId, Message.TYPE_CHALLENGE);
        m.setQuiz(quizRepository.getReferenceById(quizId));
        messageRepository.save(m);
    }

    @Transactional(readOnly = true)
    public Optional<Message> findById(int id) {
        return messageRepository.findById(id);
    }

    /** Inbox listing, most recent first, with challenge best-scores filled in. */
    @Transactional(readOnly = true)
    public List<Message> inbox(int userId, int limit) {
        List<Message> messages = messageRepository.inbox(userId, PageRequest.of(0, limit));
        for (Message m : messages) {
            if (Message.TYPE_CHALLENGE.equals(m.getType()) && m.getQuizId() != null) {
                populateChallengerBest(m);
            }
        }
        return messages;
    }

    @Transactional(readOnly = true)
    public long unreadCount(int userId) {
        return messageRepository.countByToUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(int messageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setRead(true);
            messageRepository.save(m);
        });
    }

    @Transactional
    public void delete(int messageId) {
        messageRepository.deleteById(messageId);
    }

    private void populateChallengerBest(Message m) {
        Integer fromId = m.getFromUserId();
        Integer quizId = m.getQuizId();
        Integer best = historyRepository.bestScore(fromId, quizId);
        if (best != null) {
            m.setChallengerBestScore(best);
            List<Integer> maxes = historyRepository.maxScoresForBest(fromId, quizId, PageRequest.of(0, 1));
            if (!maxes.isEmpty()) m.setChallengerBestMax(maxes.get(0));
        }
    }

    private Message baseMessage(int fromId, int toId, String type) {
        Message m = new Message();
        m.setFromUser(userRepository.getReferenceById(fromId));
        m.setToUser(userRepository.getReferenceById(toId));
        m.setType(type);
        return m;
    }
}
