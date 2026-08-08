import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'

import { Leaf, Search, UtensilsCrossed } from 'lucide-react'
import type { FoodCategory, FoodItem } from '../../../../../../types/food'
import { foodService } from '../../../../../../services/foodService'
import { foodDetailsPath } from '../../../../../../routes/routePaths'
import { CATEGORY_LABELS } from '../../../../../../constants/food'


type CategoryFilter = FoodCategory | 'ALL'

const CATEGORY_FILTERS: ReadonlyArray<{ value: CategoryFilter; label: string }> = [
  { value: 'ALL', label: 'All' },
  { value: 'FRUITS', label: 'Fruits' },
  { value: 'VEGETABLES', label: 'Vegetables' },
  { value: 'GRAINS', label: 'Grains' },
  { value: 'DAIRY', label: 'Dairy' },
  { value: 'MEAT', label: 'Meat' },
  { value: 'SEAFOOD', label: 'Seafood' },
  { value: 'LEGUMES', label: 'Legumes' },
  { value: 'NUTS_AND_SEEDS', label: 'Nuts & Seeds' },
  { value: 'BEVERAGES', label: 'Beverages' },
  { value: 'SNACKS', label: 'Snacks' },
]

const SEARCH_DELAY_MS = 300

/** Debounces a string so the API isn't called on every keystroke. */
function useDebouncedValue(value: string, delayMs: number): string {
  const [debounced, setDebounced] = useState(value)

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs)
    return () => clearTimeout(timer)
  }, [value, delayMs])

  return debounced
}

/** Fetches foods using the active filter combination. */
async function fetchFoods(category: CategoryFilter, query: string): Promise<FoodItem[]> {
  if (query && category !== 'ALL') {
    return (await foodService.searchFoods(query)).filter(
      (food) => food.category === category,
    )
  }
  if (query) {
    return foodService.searchFoods(query)
  }
  if (category !== 'ALL') {
    return foodService.getFoodsByCategory(category)
  }
  return foodService.getAllFoods()
}

function formatCalories(calories: number): string {
  return `${Math.round(calories)} kcal`
}

function formatMacro(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return 'Could not load foods. Please try again.'
}

/**
 * Food catalog — search + category filters with full loading,
 * error and empty states. Public route; no auth required.
 */
export function FoodsPage() {
  const [category, setCategory] = useState<CategoryFilter>('ALL')
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebouncedValue(search, SEARCH_DELAY_MS)

  const [foods, setFoods] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let ignore = false

    const load = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const result = await fetchFoods(category, debouncedSearch)
        if (!ignore) {
          setFoods(result)
        }
      } catch (err) {
        if (!ignore) {
          setError(getErrorMessage(err))
        }
      } finally {
        if (!ignore) {
          setIsLoading(false)
        }
      }
    }

    void load()

    return () => {
      ignore = true
    }
  }, [category, debouncedSearch, reloadKey])

  const clearFilters = () => {
    setCategory('ALL')
    setSearch('')
  }

  const hasActiveFilters = search !== '' || category !== 'ALL'

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="max-w-2xl">
        <h1 className="text-3xl font-bold tracking-tight">Food catalog</h1>
        <p className="mt-2 text-muted-foreground">
          Browse nutrient-dense foods across every category — search by name or
          filter to build meals that fit your goals and budget.
        </p>
      </header>

      {/* Search + category filter */}
      <div className="mt-8 flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="relative w-full max-w-md">
          <Search
            aria-hidden="true"
            className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
          />
          <input
            type="search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search foods…"
            aria-label="Search foods"
            className="w-full rounded-lg border border-border bg-background py-2.5 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground transition-colors focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
        </div>

        <div role="group" aria-label="Filter by category" className="flex flex-wrap gap-2">
          {CATEGORY_FILTERS.map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => setCategory(option.value)}
              aria-pressed={category === option.value}
              className={`rounded-full border px-3.5 py-1.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary ${
                category === option.value
                  ? 'border-primary bg-primary text-primary-foreground'
                  : 'border-border text-muted-foreground hover:border-primary/40 hover:text-foreground'
              }`}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>

      {/* Result count */}
      {!isLoading && !error && foods.length > 0 && (
        <p className="mt-6 text-sm text-muted-foreground">
          Showing {foods.length} {foods.length === 1 ? 'food' : 'foods'}
          {hasActiveFilters ? ' matching your filters' : ''}
        </p>
      )}

      {/* Loading skeletons */}
      {isLoading && (
        <div className="mt-6">
          <p role="status" className="sr-only">
            Loading foods…
          </p>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: 8 }, (_, index) => (
              <FoodCardSkeleton key={index} />
            ))}
          </div>
        </div>
      )}

      {/* Error state */}
      {!isLoading && error && (
        <div className="mt-6 flex flex-col items-center justify-center rounded-xl border border-border px-6 py-16 text-center">
          <h2 className="text-lg font-semibold">Couldn't load foods</h2>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">{error}</p>
          <button
            type="button"
            onClick={() => setReloadKey((key) => key + 1)}
            className="mt-5 inline-flex items-center justify-center rounded-lg border border-border bg-background px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-muted"
          >
            Try again
          </button>
        </div>
      )}

      {/* Empty state */}
      {!isLoading && !error && foods.length === 0 && (
        <div className="mt-6 flex flex-col items-center justify-center rounded-xl border border-dashed border-border px-6 py-16 text-center">
          <Search aria-hidden="true" className="h-8 w-8 text-muted-foreground" />
          <h2 className="mt-4 text-lg font-semibold">No foods found</h2>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">
            {hasActiveFilters
              ? 'No foods match your search and category. Try different keywords or clear the filters.'
              : 'The catalog is empty. Check back soon.'}
          </p>
          {hasActiveFilters && (
            <button
              type="button"
              onClick={clearFilters}
              className="mt-5 inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              Clear filters
            </button>
          )}
        </div>
      )}

      {/* Food grid */}
      {!isLoading && !error && foods.length > 0 && (
        <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {foods.map((food) => (
            <FoodCard key={food.id} food={food} />
          ))}
        </div>
      )}
    </div>
  )
}

function FoodCard({ food }: { food: FoodItem }) {
  return (
    <Link
      to={foodDetailsPath(food.id)}
      className="group flex flex-col overflow-hidden rounded-xl border border-border bg-card transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
    >
      <div className="aspect-[16/9] overflow-hidden bg-muted">
        {food.imageUrl ? (
          <img
            src={food.imageUrl}
            alt={food.name}
            loading="lazy"
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-muted-foreground">
            <UtensilsCrossed aria-hidden="true" className="h-8 w-8" />
          </div>
        )}
      </div>

      <div className="flex flex-1 flex-col p-4">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold leading-snug">{food.name}</h3>
          {food.vegetarian && (
            <span
              title="Vegetarian"
              className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-emerald-500/10 text-emerald-600 dark:text-emerald-400"
            >
              <Leaf aria-hidden="true" className="h-3.5 w-3.5" />
            </span>
          )}
        </div>

        {food.description && (
          <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
            {food.description}
          </p>
        )}

        <div className="mt-3 flex items-center justify-between">
          <span className="rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
            {CATEGORY_LABELS[food.category]}
          </span>
          <span className="text-sm font-semibold">{formatCalories(food.calories)}</span>
        </div>

        <dl className="mt-4 grid grid-cols-3 gap-2 border-t border-border pt-3">
          <MacroStat label="Protein" value={`${formatMacro(food.protein)}g`} />
          <MacroStat label="Carbs" value={`${formatMacro(food.carbohydrates)}g`} />
          <MacroStat label="Fat" value={`${formatMacro(food.fat)}g`} />
        </dl>
      </div>
    </Link>
  )
}

function MacroStat({ label, value }: { label: string; value: string }) {
  return (
    <div className="text-center">
      <dt className="text-xs text-muted-foreground">{label}</dt>
      <dd className="text-sm font-medium">{value}</dd>
    </div>
  )
}

function FoodCardSkeleton() {
  return (
    <div
      aria-hidden="true"
      className="animate-pulse overflow-hidden rounded-xl border border-border bg-card"
    >
      <div className="aspect-[16/9] bg-muted" />
      <div className="space-y-3 p-4">
        <div className="h-4 w-2/3 rounded bg-muted" />
        <div className="h-3 w-full rounded bg-muted" />
        <div className="h-3 w-4/5 rounded bg-muted" />
        <div className="h-8 w-full rounded bg-muted" />
      </div>
    </div>
  )
}