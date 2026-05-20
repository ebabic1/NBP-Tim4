package ba.unsa.etf.nbp.travel.service;

import ba.unsa.etf.nbp.travel.model.document.UserActivity;
import ba.unsa.etf.nbp.travel.repository.mongo.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

    public void logActivity(Long userId, String username, String action) {
        var activity = UserActivity.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .timestamp(LocalDateTime.now())
                .build();
        userActivityRepository.save(activity);
    }

    public List<UserActivity> findByUserId(Long userId) {
        return userActivityRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
