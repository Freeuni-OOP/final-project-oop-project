package com.quizwebsite.repository;

import com.quizwebsite.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByNameIgnoreCase(String name);

    List<Tag> findAllByOrderByNameAsc();
}
