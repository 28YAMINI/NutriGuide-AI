package com.nutriguideai.dto.request;

import com.nutriguideai.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Health vitals upsert. Every field is optional — fields omitted from a
 * partial update keep their existing values (upsert semantics).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthVitalsRequest {

    private Integer age;

    private Gender gender;

    /** Height in cm. */
    private Double height;

    /** Weight in kg. */
    private Double weight;
}