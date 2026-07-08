package com.quizwebsite.repository;

import com.quizwebsite.model.Announcement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
