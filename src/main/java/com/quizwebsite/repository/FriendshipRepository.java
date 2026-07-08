package com.quizwebsite.repository;

import com.quizwebsite.model.Friendship;
import com.quizwebsite.model.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    Optional<Friendship> findByRequesterIdAndAddresseeId(Integer requesterId, Integer addresseeId);

    boolean existsByRequesterIdAndAddresseeIdAndStatus(Integer requesterId, Integer addresseeId, String status);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
           "WHERE f.status = :status AND " +
           "((f.requesterId = :a AND f.addresseeId = :b) OR (f.requesterId = :b AND f.addresseeId = :a))")
    boolean areRelated(@Param("a") Integer a, @Param("b") Integer b, @Param("status") String status);

    @Modifying
    @Query("DELETE FROM Friendship f WHERE " +
           "(f.requesterId = :a AND f.addresseeId = :b) OR (f.requesterId = :b AND f.addresseeId = :a)")
    int deleteBetween(@Param("a") Integer a, @Param("b") Integer b);

    @Modifying
    @Query("DELETE FROM Friendship f WHERE f.requesterId = :userId OR f.addresseeId = :userId")
    int deleteAllForUser(@Param("userId") Integer userId);

    /** Friend ids where this user sent the (accepted) request. */
    @Query("SELECT f.addresseeId FROM Friendship f WHERE f.requesterId = :userId AND f.status = :status")
    List<Integer> acceptedAddresseeIds(@Param("userId") Integer userId, @Param("status") String status);

    /** Friend ids where this user received the (accepted) request. */
    @Query("SELECT f.requesterId FROM Friendship f WHERE f.addresseeId = :userId AND f.status = :status")
    List<Integer> acceptedRequesterIds(@Param("userId") Integer userId, @Param("status") String status);
}
