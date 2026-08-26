/**
 * Centralized application routes.
 *
 * Import routePaths instead of hardcoding URLs throughout the app.
 */
export const routePaths = {
  /** Public */
  home: '/',
  login: '/login',
  register: '/register',
  about: '/about',
  contact: '/contact',

  /** Protected — requires authentication */
  dashboard: '/dashboard',
  mealPlan: '/meal-plan',
  foodDiary: '/food-diary',
  progress: '/progress',
  onboarding: '/onboarding',
  foods: '/foods',
  foodDetails: '/foods/:id',
  profile: '/profile',

  /** Protected — new pages */

  /** Protected — ADMIN only (future: /admin/foods, /admin/users) */
  admin: '/admin',

  /** Catch-all fallback */
  notFound: '*',
} as const

export type RoutePath = (typeof routePaths)[keyof typeof routePaths]

/** Builds a concrete food-details URL from the route constant. */
export function foodDetailsPath(id: string | number): string {
  return routePaths.foodDetails.replace(':id', String(id))
}
