package com.nutriguideai.entity;

import com.nutriguideai.enums.FoodCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single food item in the NutriGuide AI catalog.
 *
 * <p>This is a pure persistence model — it contains schema metadata only.
 * Validation lives in the request DTOs, business rules in the service layer,
 * and this class is never exposed directly to callers (always mapped
 * through {@code FoodItemResponse}).</p>
 */
@Entity
@Table(name = "food_items",
        indexes = @Index(name = "idx_food_category", columnList = "category"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoodCategory category;

    @Column(nullable = false, precision = 6, scale = 2)
    private Double calories;

    @Column(precision = 6, scale = 2)
    private Double protein;

    @Column(precision = 6, scale = 2)
    private Double carbohydrates;

    @Column(precision = 6, scale = 2)
    private Double fat;

    @Column(precision = 6, scale = 2)
    private Double fiber;

    @Column(name = "serving_size", nullable = false, length = 50)
    private String servingSize;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean vegetarian;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}