package com.quizwebsite.model.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A friendship request / relationship. Maps to {@code friendships} with the
 * composite key (requester_id, addressee_id).
 *
 *   request  → row inserted with status = PENDING
 *   accept   → status updated to ACCEPTED
 *   decline  → row deleted
 *
 * "Friends" means an ACCEPTED row exists in either direction.
 */
@Entity
@Table(name = "friendships")
@IdClass(FriendshipId.class)
@Getter
@Setter
@NoArgsConstructor
public class Friendship {

    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";

    @Id
    @Column(name = "requester_id")
    private Integer requesterId;

    @Id
    @Column(name = "addressee_id")
    private Integer addresseeId;

    @Column(nullable = false, length = 20)
    private String status = STATUS_PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Friendship(Integer requesterId, Integer addresseeId, String status) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
    }
}
