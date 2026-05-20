package ba.unsa.etf.nbp.travel.controller;

import ba.unsa.etf.nbp.travel.dto.response.ActivityResponse;
import ba.unsa.etf.nbp.travel.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final UserActivityService userActivityService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<ActivityResponse>> getActivity(@PathVariable Long userId) {
        var activities = userActivityService.findByUserId(userId);
        var response = activities.stream()
                .map(a -> new ActivityResponse(a.getUserId(), a.getUsername(), a.getAction(), a.getTimestamp()))
                .toList();
        return ResponseEntity.ok(response);
    }
}
