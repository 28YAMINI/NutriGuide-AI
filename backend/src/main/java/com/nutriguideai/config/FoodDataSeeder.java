package com.nutriguideai.config;

import com.nutriguideai.entity.FoodItem;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodDataSeeder implements CommandLineRunner {

    private final FoodItemRepository foodItemRepository;

    @Override
    public void run(String... args) {

        // Do not create duplicates on normal application restarts.
        if (foodItemRepository.count() > 0) {
            return;
        }

        List<FoodItem> foods = List.of(
                food(
                        "Apple",
                        "Fresh red apple",
                        FoodCategory.FRUIT,
                        52.0,
                        0.3,
                        13.8,
                        0.2,
                        2.4,
                        "100 g",
                        "/images/foods/apple.jpg",
                        true
                ),
                food(
                        "Banana",
                        "Ripe banana",
                        FoodCategory.FRUIT,
                        89.0,
                        1.1,
                        22.8,
                        0.3,
                        2.6,
                        "100 g",
                        "/images/foods/banana-1025109_1280.jpg",
                        true
                ),
                food(
                        "Orange",
                        "Fresh orange",
                        FoodCategory.FRUIT,
                        47.0,
                        0.9,
                        11.8,
                        0.1,
                        2.4,
                        "100 g",
                        "/images/foods/orange.jpg",
                        true
                ),
                food(
                        "Mango",
                        "Sweet ripe mango",
                        FoodCategory.FRUIT,
                        60.0,
                        0.8,
                        15.0,
                        0.4,
                        1.6,
                        "100 g",
                        "/images/foods/mango.jpg",
                        true
                ),
                food(
                        "Grapes",
                        "Fresh grapes",
                        FoodCategory.FRUIT,
                        69.0,
                        0.7,
                        18.1,
                        0.2,
                        0.9,
                        "100 g",
                        "/images/foods/grapes.jpg",
                        true
                ),

                food(
                        "Spinach",
                        "Fresh spinach leaves",
                        FoodCategory.VEGETABLE,
                        23.0,
                        2.9,
                        3.6,
                        0.4,
                        2.2,
                        "100 g",
                        "/images/foods/spinach.jpg",
                        true
                ),
                food(
                        "Tomato",
                        "Fresh tomato",
                        FoodCategory.VEGETABLE,
                        18.0,
                        0.9,
                        3.9,
                        0.2,
                        1.2,
                        "100 g",
                        "/images/foods/tomato.jpg",
                        true
                ),
                food(
                        "Broccoli",
                        "Fresh broccoli florets",
                        FoodCategory.VEGETABLE,
                        34.0,
                        2.8,
                        6.6,
                        0.4,
                        2.6,
                        "100 g",
                        "/images/foods/broccoli.jpg",
                        true
                ),
                food(
                        "Carrot",
                        "Fresh carrot",
                        FoodCategory.VEGETABLE,
                        41.0,
                        0.9,
                        9.6,
                        0.2,
                        2.8,
                        "100 g",
                        "/images/foods/carrot.jpg",
                        true
                ),
                food(
                        "Potato",
                        "Boiled potato",
                        FoodCategory.VEGETABLE,
                        77.0,
                        2.0,
                        17.5,
                        0.1,
                        2.2,
                        "100 g",
                        "/images/foods/potato.jpg",
                        true
                ),

                food(
                        "Oats",
                        "Rolled oats",
                        FoodCategory.GRAIN,
                        389.0,
                        16.9,
                        67.7,
                        6.9,
                        10.1,
                        "100 g",
                        "/images/foods/oats.jpg",
                        true
                ),
                food(
                        "Rice",
                        "Cooked rice",
                        FoodCategory.GRAIN,
                        130.0,
                        2.7,
                        28.2,
                        0.3,
                        0.4,
                        "100 g",
                        "/images/foods/rice.jpg",
                        true
                ),
                food(
                        "Bread",
                        "Whole grain bread",
                        FoodCategory.GRAIN,
                        247.0,
                        13.0,
                        41.0,
                        3.4,
                        6.0,
                        "100 g",
                        "/images/foods/bread.jpg",
                        true
                ),

                food(
                        "Milk",
                        "Milk",
                        FoodCategory.DAIRY,
                        61.0,
                        3.2,
                        4.7,
                        3.3,
                        0.0,
                        "100 ml",
                        "/images/foods/milk.jpg",
                        true
                ),
                food(
                        "Curd",
                        "Fresh curd",
                        FoodCategory.DAIRY,
                        98.0,
                        3.5,
                        4.0,
                        5.0,
                        0.0,
                        "100 g",
                        "/images/foods/Curd.jpg",
                        true
                ),
                food(
                        "Paneer",
                        "Paneer cheese",
                        FoodCategory.DAIRY,
                        321.0,
                        18.3,
                        3.6,
                        25.0,
                        0.0,
                        "100 g",
                        "/images/foods/panner.jpg",
                        true
                ),
                food(
                        "Cheese",
                        "Cheese",
                        FoodCategory.DAIRY,
                        402.0,
                        25.0,
                        1.3,
                        33.0,
                        0.0,
                        "100 g",
                        "/images/foods/cheese.jpg",
                        true
                ),

                food(
                        "Eggs",
                        "Eggs",
                        FoodCategory.PROTEIN,
                        155.0,
                        12.6,
                        1.1,
                        10.6,
                        0.0,
                        "100 g",
                        "/images/foods/eggs.jpg",
                        false
                ),
                food(
                        "Chicken",
                        "Chicken meat",
                        FoodCategory.MEAT,
                        165.0,
                        31.0,
                        0.0,
                        3.6,
                        0.0,
                        "100 g",
                        "/images/foods/chicken.jpg",
                        false
                ),
                food(
                        "Fish",
                        "Fish fillet",
                        FoodCategory.MEAT,
                        206.0,
                        22.0,
                        0.0,
                        4.5,
                        0.0,
                        "100 g",
                        "/images/foods/fish.jpg",
                        false
                ),

                food(
                        "Lentils",
                        "Cooked lentils",
                        FoodCategory.LEGUME,
                        116.0,
                        9.0,
                        20.1,
                        0.4,
                        7.9,
                        "100 g",
                        "/images/foods/lentils.jpg",
                        true
                ),
                food(
                        "Chickpeas",
                        "Cooked chickpeas",
                        FoodCategory.LEGUME,
                        164.0,
                        8.9,
                        27.4,
                        2.6,
                        7.6,
                        "100 g",
                        "/images/foods/chickpeas.jpg",
                        true
                ),

                food(
                        "Almonds",
                        "Almonds",
                        FoodCategory.NUTS_SEEDS,
                        579.0,
                        21.2,
                        21.6,
                        49.9,
                        12.5,
                        "100 g",
                        "/images/foods/almonds.jpg",
                        true
                ),
                food(
                        "Peanuts",
                        "Peanuts",
                        FoodCategory.NUTS_SEEDS,
                        567.0,
                        25.8,
                        16.1,
                        49.2,
                        8.5,
                        "100 g",
                        "/images/foods/peasnuts.jpg",
                        true
                ),

                food(
                        "Green Tea",
                        "Brewed green tea",
                        FoodCategory.BEVERAGE,
                        1.0,
                        0.2,
                        0.2,
                        0.0,
                        0.0,
                        "240 ml",
                        "/images/foods/greentea.jpg",
                        true
                ),
                food(
                        "Coffee",
                        "Brewed coffee",
                        FoodCategory.BEVERAGE,
                        2.0,
                        0.3,
                        0.0,
                        0.0,
                        0.0,
                        "240 ml",
                        "/images/foods/coffee.jpg",
                        true
                ),

                food(
                        "Coconut Oil",
                        "Coconut oil",
                        FoodCategory.OIL,
                        884.0,
                        0.0,
                        0.0,
                        100.0,
                        0.0,
                        "100 ml",
                        "/images/foods/coconut oil.jpg",
                        true
                ),
                food(
                        "Turmeric",
                        "Ground turmeric",
                        FoodCategory.SPICE,
                        312.0,
                        9.7,
                        67.1,
                        3.2,
                        22.7,
                        "100 g",
                        "/images/foods/turmeric.jpg",
                        true
                )
        );

        foodItemRepository.saveAll(foods);
    }

    private FoodItem food(
            String name,
            String description,
            FoodCategory category,
            Double calories,
            Double protein,
            Double carbohydrates,
            Double fat,
            Double fiber,
            String servingSize,
            String imageUrl,
            Boolean vegetarian
    ) {
        return FoodItem.builder()
                .name(name)
                .description(description)
                .category(category)
                .calories(calories)
                .protein(protein)
                .carbohydrates(carbohydrates)
                .fat(fat)
                .fiber(fiber)
                .servingSize(servingSize)
                .imageUrl(imageUrl)
                .vegetarian(vegetarian)
                .build();
    }
}