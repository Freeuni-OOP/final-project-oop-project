package com.quizwebsite.model.social;

import com.quizwebsite.model.Quiz;
import com.quizwebsite.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row from the {@code messages} table.
 *
 * Type values: {@code FRIEND_REQUEST}, {@code CHALLENGE}, {@code NOTE}.
 *
 * CHALLENGE messages have {@code quiz} set; {@code challengerBestScore/Max} are
 * populated by the service so the inbox can render
 * "X challenged you to Y (their best: N/M)" in one shot.
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_to", columnList = "to_user, created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class Message {

    public static final String TYPE_FRIEND_REQUEST = "FRIEND_REQUEST";
    public static final String TYPE_CHALLENGE      = "CHALLENGE";
    public static final String TYPE_NOTE           = "NOTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user", nullable = false)
    private User toUser;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;       // null for FRIEND_REQUEST / NOTE

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ---- computed, set by the service for the inbox view ----
    @Transient
    private Integer challengerBestScore;
    @Transient
    private Integer challengerBestMax;

    // ---- convenience accessors used by the views ----

    @Transient
    public Integer getFromUserId() { return fromUser == null ? null : fromUser.getId(); }

    @Transient
    public String getFromUsername() { return fromUser == null ? null : fromUser.getUsername(); }

    @Transient
    public Integer getToUserId() { return toUser == null ? null : toUser.getId(); }

    @Transient
    public Integer getQuizId() { return quiz == null ? null : quiz.getId(); }

    @Transient
    public String getQuizName() { return quiz == null ? null : quiz.getName(); }
}
