package com.quizwebsite.repository;

import com.quizwebsite.model.Quiz;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {

    /** Loads a quiz with its questions eagerly (answers are initialized separately). */
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Integer id);

    List<Quiz> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Quiz> findByCategoryIdOrderByCreatedAtDesc(Integer categoryId);

    @Query("SELECT q FROM Quiz q JOIN q.tags t WHERE t.id = :tagId ORDER BY q.createdAt DESC")
    List<Quiz> findByTagIdOrderByCreatedAtDesc(@Param("tagId") Integer tagId);

    @Query("SELECT q FROM Quiz q WHERE q.creator.id = :creatorId ORDER BY q.createdAt DESC")
    List<Quiz> findByCreatorIdOrderByCreatedAtDesc(@Param("creatorId") Integer creatorId);

    @Query("SELECT q FROM Quiz q WHERE q.creator.id IN :creatorIds ORDER BY q.createdAt DESC")
    List<Quiz> findByCreatorIdInOrderByCreatedAtDesc(@Param("creatorIds") List<Integer> creatorIds,
                                                      Pageable pageable);

    @Query("""
           SELECT DISTINCT q FROM Quiz q
           LEFT JOIN q.category c
           LEFT JOIN q.tags t
           WHERE (:query IS NULL OR
                  LOWER(q.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
                  LOWER(COALESCE(q.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))
             AND (:category IS NULL OR LOWER(c.name) = LOWER(:category))
             AND (:tag IS NULL OR LOWER(t.name) = LOWER(:tag))
           ORDER BY q.createdAt DESC
           """)
    List<Quiz> search(@Param("query") String query,
                      @Param("category") String category,
                      @Param("tag") String tag,
                      Pageable pageable);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.creator.id = :creatorId")
    long countByCreatorId(@Param("creatorId") Integer creatorId);
}
