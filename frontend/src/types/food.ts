

/**
 * Food category values — must match the backend FoodCategory enum.
 */

export type FoodCategory =
    | 'VEGETABLE'
    | 'FRUIT'
    | 'GRAIN'
    | 'PROTEIN'
    | 'DAIRY'
    | 'LEGUME'
    | 'NUTS_SEEDS'
    | 'SNACK'
    | 'BEVERAGE'
    | 'OIL'
    | 'SPICE'
    | 'MEAT'
    | 'OTHER'

export interface FoodItem {
  id: number
  name: string
  description: string
  category: FoodCategory
  calories: number
  protein: number
  carbohydrates: number
  fat: number
  fiber: number
  servingSize: string
  imageUrl: string | null
  vegetarian: boolean
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