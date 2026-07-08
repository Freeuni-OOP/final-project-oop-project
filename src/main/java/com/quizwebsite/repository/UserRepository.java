package com.quizwebsite.repository;

import com.quizwebsite.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Case-insensitive substring match for the user-lookup page. */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY u.username")
    List<User> searchByUsername(@Param("q") String query, Pageable pageable);

    List<User> findAllByOrderByUsernameAsc();
}
