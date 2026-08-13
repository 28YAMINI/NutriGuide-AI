import type { FoodCategory } from '../types/food'

/**
 * Human-readable labels for every food category.
 *
 * Keyed by the exact enum values the Spring Boot backend returns, so
 * the frontend never hardcodes a category string.
 */
export const CATEGORY_LABELS: Record<FoodCategory, string> = {
  FRUITS: 'Fruits',
  VEGETABLES: 'Vegetables',
  GRAINS: 'Grains',
  DAIRY: 'Dairy',
  MEAT: 'Meat',
  SEAFOOD: 'Seafood',
  LEGUMES: 'Legumes',
  NUTS_AND_SEEDS: 'Nuts & Seeds',
  BEVERAGES: 'Beverages',
  SNACKS: 'Snacks',
}

/**
 * All category keys in display order.
 *
 * Derived from CATEGORY_LABELS so there is one source of truth —
 * add a category once and labels, filter chips and any category list
 * all update together.
 */
export const FOOD_CATEGORIES = Object.keys(CATEGORY_LABELS) as FoodCategory[]