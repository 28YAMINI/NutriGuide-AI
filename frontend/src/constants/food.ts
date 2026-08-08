import type { FoodCategory } from '../types/food'

/**
 * Display labels for every food category.
 * Shared by FoodsPage and FoodDetailsPage.
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