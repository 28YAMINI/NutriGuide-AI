/**
 * Food category values — must match the backend FoodCategory enum.
 */
export type FoodCategory =
  | 'FRUITS'
  | 'VEGETABLES'
  | 'GRAINS'
  | 'DAIRY'
  | 'MEAT'
  | 'SEAFOOD'
  | 'LEGUMES'
  | 'NUTS_AND_SEEDS'
  | 'BEVERAGES'
  | 'SNACKS'

/**
 * A food item as returned by the backend (FoodItemResponse DTO).
 * All macro fields are per serving.
 */
export interface FoodItem {
  id: number
  name: string
  description: string
  category: FoodCategory
  /** Energy per serving, in kilocalories. */
  calories: number
  /** Grams per serving. */
  protein: number
  /** Grams per serving. */
  carbohydrates: number
  /** Grams per serving. */
  fat: number
  /** Grams per serving. */
  fiber: number
  /** Human-readable serving size, e.g. "100 g" or "1 cup". */
  servingSize: string
  /** Null when the admin did not provide an image. */
  imageUrl: string | null
  vegetarian: boolean
  createdAt: string
  updatedAt: string
}

/**
 * Payload for POST /api/foods — matches CreateFoodItemRequest.
 * Used by the Admin page (Step 21).
 */
export interface CreateFoodRequest {
  name: string
  description: string
  category: FoodCategory
  calories: number
  protein: number
  carbohydrates: number
  fat: number
  fiber: number
  servingSize: string
  imageUrl?: string
  vegetarian: boolean
}

/**
 * Payload for PUT /api/foods/{id} — matches UpdateFoodItemRequest.
 * Used by the Admin page (Step 21).
 */
export interface UpdateFoodRequest extends CreateFoodRequest {}