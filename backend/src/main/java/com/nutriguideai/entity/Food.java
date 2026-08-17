package com.nutriguideai.entity;

import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "foods")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoodCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Region region;

    /** Nutritional values are per 100 g / 100 ml. */
    @Column(nullable = false)
    private Double calories;

    @Column(nullable = false)
    private Double protein;

    @Column(nullable = false)
    private Double carbs;

    @Column(nullable = false)
    private Double fat;

    @Column(nullable = false)
    private Double fiber;

    @Column(nullable = false)
    private Boolean vegetarian;

    @Column(nullable = false)
    private Boolean vegan;

    /** Comma-separated allergens, e.g. "gluten, dairy, peanuts". Nullable. */
    @Column(length = 255)
    private String allergens;

    /** Soft delete: admin disables a food instead of hard-deleting it. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}