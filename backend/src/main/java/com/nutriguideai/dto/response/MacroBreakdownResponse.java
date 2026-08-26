package com.nutriguideai.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacroBreakdownResponse {

    private Double proteinConsumedG;
    private Double proteinTargetG;
    private Double proteinPercent;

    private Double carbsConsumedG;
    private Double carbsTargetG;
    private Double carbsPercent;

    private Double fatConsumedG;
    private Double fatTargetG;
    private Double fatPercent;
}