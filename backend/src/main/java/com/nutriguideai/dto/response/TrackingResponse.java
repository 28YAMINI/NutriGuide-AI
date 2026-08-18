package com.nutriguideai.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {

    private Long id;
    private LocalDate recordedDate;
    private Double weightKg;
    private Double bmi;
    private Integer waterIntakeMl;
    private Double sleepHours;
    private String notes;
}