import type {
  CreateFoodRequest,
  FoodCategory,
  FoodItem,
  UpdateFoodRequest,
} from '../types/food'
import api from './api'


/**
 * Food module API calls.
 *
 * GET endpoints are public; POST/PUT/DELETE require an ADMIN token —
 * the axios request interceptor attaches it automatically.
 */
export const foodService = {
  /** GET /api/foods — full catalog. */
  getAllFoods(): Promise<FoodItem[]> {
    return api.get<FoodItem[]>('/foods').then((response) => response.data)
  },

  /** GET /api/foods/{id}. */
  getFoodById(id: number | string): Promise<FoodItem> {
    return api.get<FoodItem>(`/foods/${id}`).then((response) => response.data)
  },

  /** GET /api/foods/category/{category}. */
  getFoodsByCategory(category: FoodCategory): Promise<FoodItem[]> {
    return api
      .get<FoodItem[]>(`/foods/category/${category}`)
      .then((response) => response.data)
  },

  /** GET /api/foods/search?name={query}. */
  searchFoods(query: string): Promise<FoodItem[]> {
    return api
      .get<FoodItem[]>('/foods/search', { params: { name: query } })
      .then((response) => response.data)
  },

  /** POST /api/foods — ADMIN only (used by the Admin page, Step 21). */
  createFood(payload: CreateFoodRequest): Promise<FoodItem> {
    return api.post<FoodItem>('/foods', payload).then((response) => response.data)
  },

  /** PUT /api/foods/{id} — ADMIN only (used by the Admin page, Step 21). */
  updateFood(id: number | string, payload: UpdateFoodRequest): Promise<FoodItem> {
    return api.put<FoodItem>(`/foods/${id}`, payload).then((response) => response.data)
  },

  /** DELETE /api/foods/{id} — ADMIN only (used by the Admin page, Step 21). */
  deleteFood(id: number | string): Promise<void> {
    return api.delete(`/foods/${id}`).then(() => undefined)
  },
}