/**
 * Food catalog constants.
 *
 * These values must match the backend FoodCategory enum exactly.
 */
export const FOOD_CATEGORIES = [
  'VEGETABLE',
  'FRUIT',
  'GRAIN',
  'PROTEIN',
  'DAIRY',
  'LEGUME',
  'NUTS_SEEDS',
  'SNACK',
  'BEVERAGE',
  'OIL',
  'SPICE',
  'MEAT',
  'OTHER',
] as const

export type FoodCategory = (typeof FOOD_CATEGORIES)[number]

export const CATEGORY_LABELS: Record<FoodCategory, string> = {
  VEGETABLE: 'Vegetables',
  FRUIT: 'Fruits',
  GRAIN: 'Grains',
  PROTEIN: 'Protein',
  DAIRY: 'Dairy',
  LEGUME: 'Legumes',
  NUTS_SEEDS: 'Nuts & Seeds',
  SNACK: 'Snacks',
  BEVERAGE: 'Beverages',
  OIL: 'Oils',
  SPICE: 'Spices',
  MEAT: 'Meat',
  OTHER: 'Other',
}

export const CATEGORY_EMOJI: Record<FoodCategory, string> = {
  VEGETABLE: '🥦',
  FRUIT: '🍎',
  GRAIN: '🌾',
  PROTEIN: '🍗',
  DAIRY: '🥛',
  LEGUME: '🫘',
  NUTS_SEEDS: '🥜',
  SNACK: '🍫',
  BEVERAGE: '🍵',
  OIL: '🫒',
  SPICE: '🌿',
  MEAT: '🥩',
  OTHER: '🥗',
}

/**
 * Local fallback artwork.
 *
 * Real food JPGs are handled by foodImages.ts.
 * These are only used when a food-specific image is unavailable.
 */
export const CATEGORY_FALLBACK_IMAGE: Record<FoodCategory, string> = {
  VEGETABLE: '/images/foods/spinach.jpg',
  FRUIT: '/images/foods/curd.jpg',
  GRAIN: '/images/foods/oats.jpg',
  PROTEIN: '/images/foods/chicken.jpg',
  DAIRY: '/images/foods/milk.jpg',
  LEGUME: '/images/foods/lentils.jpg',
  NUTS_SEEDS: '/images/foods/almonds.jpg',
  SNACK: '/images/foods/peanuts.jpg',
  BEVERAGE: '/images/foods/green-tea.jpg',
  OIL: '/images/foods/coconut-oil.jpg',
  SPICE: '/images/foods/turmeric.jpg',
  MEAT: '/images/foods/chicken.jpg',
  OTHER: '/images/foods/curd.jpg',
}

export const GENERIC_FALLBACK_IMAGE = '/images/foods/curd.jpg'

export const SORT_OPTIONS = [
  { value: 'name', label: 'Name (A–Z)' },
  { value: 'calories', label: 'Calories (low to high)' },
  { value: 'protein', label: 'Protein (high to low)' },
  { value: 'fat', label: 'Fat (low to high)' },
] as const

export type FoodSort = (typeof SORT_OPTIONS)[number]['value']