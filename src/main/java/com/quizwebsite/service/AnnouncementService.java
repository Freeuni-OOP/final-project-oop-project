package com.quizwebsite.service;

import com.quizwebsite.model.Announcement;
import com.quizwebsite.repository.AnnouncementRepository;
import com.quizwebsite.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Admin announcements shown on the home page. */
@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(int adminId, String body) {
        announcementRepository.save(new Announcement(userRepository.getReferenceById(adminId), body));
    }

    @Transactional(readOnly = true)
    public List<Announcement> listRecent(int limit) {
        return announcementRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional
    public void delete(int id) {
        announcementRepository.deleteById(id);
    }
}
