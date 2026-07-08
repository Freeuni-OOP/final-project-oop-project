package com.quizwebsite.service;

import com.quizwebsite.model.Friendship;
import com.quizwebsite.model.Message;
import com.quizwebsite.model.User;
import com.quizwebsite.repository.FriendshipRepository;
import com.quizwebsite.repository.MessageRepository;
import com.quizwebsite.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manages friendships together with the FRIEND_REQUEST messages that drive the
 * spec's two-step add / accept flow.
 *
 *   request → friendships(PENDING) + a FRIEND_REQUEST message
 *   accept  → friendships → ACCEPTED, remove the message
 *   decline → delete the pending friendship + the message
 *   remove  → delete the friendship (either direction)
 *
 * "Friends" means an ACCEPTED row exists in either direction.
 */
@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             MessageRepository messageRepository,
                             UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /** No-op if same user, already friends, or a request is pending either way. */
    @Transactional
    public boolean request(int requesterId, int addresseeId) {
        if (requesterId == addresseeId) return false;
        if (areFriends(requesterId, addresseeId)) return false;
        if (pendingFromTo(requesterId, addresseeId)) return false;
        if (pendingFromTo(addresseeId, requesterId)) return false;

        friendshipRepository.save(new Friendship(requesterId, addresseeId, Friendship.STATUS_PENDING));

        Message msg = new Message();
        msg.setFromUser(userRepository.getReferenceById(requesterId));
        msg.setToUser(userRepository.getReferenceById(addresseeId));
        msg.setType(Message.TYPE_FRIEND_REQUEST);
        messageRepository.save(msg);
        return true;
    }

    @Transactional
    public boolean accept(int requesterId, int addresseeId) {
        Optional<Friendship> opt = friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId);
        if (opt.isEmpty() || !Friendship.STATUS_PENDING.equals(opt.get().getStatus())) {
            return false;
        }
        opt.get().setStatus(Friendship.STATUS_ACCEPTED);
        friendshipRepository.save(opt.get());
        messageRepository.deleteByFromToAndType(requesterId, addresseeId, Message.TYPE_FRIEND_REQUEST);
        return true;
    }

    @Transactional
    public boolean decline(int requesterId, int addresseeId) {
        Optional<Friendship> opt = friendshipRepository.findByRequesterIdAndAddresseeId(requesterId, addresseeId);
        boolean removed = false;
        if (opt.isPresent() && Friendship.STATUS_PENDING.equals(opt.get().getStatus())) {
            friendshipRepository.delete(opt.get());
            removed = true;
        }
        messageRepository.deleteByFromToAndType(requesterId, addresseeId, Message.TYPE_FRIEND_REQUEST);
        return removed;
    }

    @Transactional
    public void remove(int userIdA, int userIdB) {
        friendshipRepository.deleteBetween(userIdA, userIdB);
    }

    @Transactional(readOnly = true)
    public boolean areFriends(int a, int b) {
        if (a == b) return false;
        return friendshipRepository.areRelated(a, b, Friendship.STATUS_ACCEPTED);
    }

    @Transactional(readOnly = true)
    public boolean pendingFromTo(int from, int to) {
        return friendshipRepository.existsByRequesterIdAndAddresseeIdAndStatus(from, to, Friendship.STATUS_PENDING);
    }

    @Transactional(readOnly = true)
    public List<User> listFriends(int userId) {
        Set<Integer> ids = new LinkedHashSet<>(listFriendIds(userId));
        if (ids.isEmpty()) return List.of();
        List<User> friends = userRepository.findAllById(ids);
        friends.sort(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
        return friends;
    }

    @Transactional(readOnly = true)
    public List<Integer> listFriendIds(int userId) {
        Set<Integer> ids = new LinkedHashSet<>();
        ids.addAll(friendshipRepository.acceptedAddresseeIds(userId, Friendship.STATUS_ACCEPTED));
        ids.addAll(friendshipRepository.acceptedRequesterIds(userId, Friendship.STATUS_ACCEPTED));
        return List.copyOf(ids);
    }
}
