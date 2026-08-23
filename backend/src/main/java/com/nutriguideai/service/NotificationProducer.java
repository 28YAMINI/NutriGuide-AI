package com.nutriguideai.service;

import com.nutriguideai.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:nutriguide.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey:notification.routingkey}")
    private String routingKey;

    public void sendNotification(NotificationMessage message) {
        log.info("Publishing notification to RabbitMQ for user={}: {}", message.getUserId(), message.getTitle());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}