package com.quizwebsite.model.social;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key for {@link Friendship}: (requesterId, addresseeId). */
public class FriendshipId implements Serializable {

    private Integer requesterId;
    private Integer addresseeId;

    public FriendshipId() {}

    public FriendshipId(Integer requesterId, Integer addresseeId) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipId that)) return false;
        return Objects.equals(requesterId, that.requesterId)
                && Objects.equals(addresseeId, that.addresseeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requesterId, addresseeId);
    }
}
