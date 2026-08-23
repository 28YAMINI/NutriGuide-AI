package com.nutriguideai.service;

import com.nutriguideai.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_NOTIFICATION_PREFIX = "user:notifications:";

    @RabbitListener(queues = "${app.rabbitmq.queue:notification.queue}")
    public void consumeNotification(NotificationMessage notification) {
        log.info("Received notification from RabbitMQ for userId={}: {}", notification.getUserId(), notification.getTitle());

        // Store into Redis list for fast sub-millisecond retrieval (keep last 50 notifications, expire after 7 days)
        String userKey = REDIS_NOTIFICATION_PREFIX + notification.getUserId();
        redisTemplate.opsForList().leftPush(userKey, notification);
        redisTemplate.opsForList().trim(userKey, 0, 49);
        redisTemplate.expire(userKey, 7, TimeUnit.DAYS);

        log.info("Saved notification to Redis cache for userId={}", notification.getUserId());
    }
}