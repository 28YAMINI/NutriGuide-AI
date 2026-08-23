package com.nutriguideai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private String id;
    private Long userId;
    private String title;
    private String message;
    private String type; // e.g., "MEAL_PLAN_READY", "WATER_REMINDER", "GOAL_REACHED"
    private boolean isRead;
    private LocalDateTime timestamp;
}