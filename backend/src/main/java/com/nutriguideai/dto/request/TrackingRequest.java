package com.nutriguideai.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingRequest {

    @Positive(message = "Weight must be positive")
    @Max(500)
    private Double weightKg;

    @Min(200) @Max(10000)
    private Integer waterIntakeMl;

    @Min(0) @Max(24)
    private Double sleepHours;

    @Size(max = 500)
    private String notes;
}