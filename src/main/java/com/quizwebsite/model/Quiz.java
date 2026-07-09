package com.quizwebsite.model;

import com.quizwebsite.model.question.Question;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** A quiz and its options. Maps to the {@code quizzes} table. */
@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(name = "quiz_tags",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @OrderBy("name ASC")
    private Set<Tag> tags = new LinkedHashSet<>();

    @Column(name = "is_random", nullable = false)
    private boolean randomQuestions;

    @Column(name = "is_multi_page", nullable = false)
    private boolean multiPage;

    @Column(name = "is_immediate_correction", nullable = false)
    private boolean immediateCorrection;

    @Column(name = "is_practice_enabled", nullable = false)
    private boolean practiceEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Question> questions = new ArrayList<>();

    /** Adds a question and wires the back-reference + next position. */
    public void addQuestion(Question q) {
        q.setQuiz(this);
        q.setPosition(questions.size());
        questions.add(q);
    }

    // ---- convenience accessors used by the views (mirror the old joined fields) ----

    @Transient
    public Integer getCreatorId() {
        return creator == null ? null : creator.getId();
    }

    @Transient
    public String getCreatorUsername() {
        return creator == null ? null : creator.getUsername();
    }

    @Transient
    public String getCategoryName() {
        return category == null ? null : category.getName();
    }

    @Transient
    public String getTagsText() {
        return tags.stream().map(Tag::getName).collect(Collectors.joining(", "));
    }

    /** Sum of max scores across every question — used by the results page. */
    @Transient
    public int getMaxScore() {
        int total = 0;
        for (Question q : questions) total += q.getMaxScore();
        return total;
    }
}
