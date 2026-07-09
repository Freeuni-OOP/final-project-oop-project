package com.quizwebsite.service;

import com.quizwebsite.model.PrivacySetting;
import com.quizwebsite.model.User;
import com.quizwebsite.model.activity.HistoryActivity;
import com.quizwebsite.model.activity.QuizHistoryEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** Centralizes the profile-history privacy rules. */
@Service
public class PrivacyService {

    private final FriendshipService friendshipService;

    public PrivacyService(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @Transactional(readOnly = true)
    public boolean canViewHistory(User viewer, User profile) {
        if (viewer == null || profile == null) return false;
        if (viewer.getId().equals(profile.getId()) || viewer.isAdmin()) return true;
        PrivacySetting setting = profile.getPrivacySetting();
        if (setting == null || setting == PrivacySetting.PUBLIC) return true;
        if (setting == PrivacySetting.PRIVATE) return false;
        return friendshipService.areFriends(viewer.getId(), profile.getId());
    }

    @Transactional(readOnly = true)
    public List<HistoryActivity> historyActivities(User viewer, List<QuizHistoryEntry> entries) {
        return historyActivities(viewer, entries, false);
    }

    @Transactional(readOnly = true)
    public List<HistoryActivity> visibleHistoryActivities(User viewer, List<QuizHistoryEntry> entries) {
        return historyActivities(viewer, entries, true);
    }

    private List<HistoryActivity> historyActivities(User viewer, List<QuizHistoryEntry> entries, boolean omitHidden) {
        List<HistoryActivity> out = new ArrayList<>();
        if (entries == null) return out;
        for (QuizHistoryEntry entry : entries) {
            boolean visible = canViewHistory(viewer, entry.getUser());
            if (omitHidden && !visible) continue;
            out.add(new HistoryActivity(entry, visible));
        }
        return out;
    }
}
