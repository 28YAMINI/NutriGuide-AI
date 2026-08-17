package com.nutriguideai.repository;

import com.nutriguideai.entity.Food;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findByIdAndActiveTrue(Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT f FROM Food f WHERE f.active = true "
            + "AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:category IS NULL OR f.category = :category) "
            + "AND (:region IS NULL OR f.region = :region) "
            + "AND (:vegetarian IS NULL OR f.vegetarian = :vegetarian) "
            + "ORDER BY f.name ASC")
    List<Food> search(@Param("keyword") String keyword,
                      @Param("category") FoodCategory category,
                      @Param("region") Region region,
                      @Param("vegetarian") Boolean vegetarian);
}