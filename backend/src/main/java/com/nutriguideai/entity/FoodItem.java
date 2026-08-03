package com.nutriguideai.entity;

import com.nutriguideai.enums.FoodCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "food_items",
        indexes = {
                @Index(name = "idx_food_category", columnList = "category")
        }
)
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
    @Column(nullable = false)
    private FoodCategory category;

    @Column(nullable = false)
    private Double calories;

    @Column
    private Double protein;

    @Column
    private Double carbohydrates;

    @Column
    private Double fat;

    @Column
    private Double fiber;

    @Column(nullable = false, length = 50)
    private String servingSize;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean vegetarian;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}