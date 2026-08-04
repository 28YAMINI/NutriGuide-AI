package com.nutriguideai.repository;


import com.nutriguideai.entity.FoodItem;
import com.nutriguideai.enums.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the FoodItem entity.
 *
 * <p>Inherits standard CRUD (save, findById, findAll, deleteById) and adds the
 * four derived query methods the FoodItemService needs. Method names are parsed
 * into SQL at runtime — no JPQL or native queries required.</p>
 */
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    /** All foods in a category, used by GET /api/foods/category/{category}. */
    List<FoodItem> findByCategory(FoodCategory category);

    /** Case-insensitive substring search, used by GET /api/foods/search?name=. */
    List<FoodItem> findByNameContainingIgnoreCase(String name);

    /** Duplicate guard for CREATE — case-insensitive, whitespace already trimmed by service. */
    boolean existsByNameIgnoreCase(String name);

    /** Duplicate guard for UPDATE — same check, but excludes the item being edited. */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}