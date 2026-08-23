package com.nutriguideai.controller;

import com.nutriguideai.dto.NotificationMessage;
import com.nutriguideai.entity.User;
import com.nutriguideai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private static final String REDIS_NOTIFICATION_PREFIX = "user:notifications:";

    @GetMapping
    public ResponseEntity<List<NotificationMessage>> getMyNotifications(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String key = REDIS_NOTIFICATION_PREFIX + user.getId();
        List<Object> rawList = redisTemplate.opsForList().range(key, offset, offset + limit - 1);

        if (rawList == null) {
            return ResponseEntity.ok(List.of());
        }

        List<NotificationMessage> notifications = rawList.stream()
                .filter(obj -> obj instanceof NotificationMessage)
                .map(obj -> (NotificationMessage) obj)
                .collect(Collectors.toList());

        return ResponseEntity.ok(notifications);
    }
}