import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'

import {
  ChevronLeft,
  ChevronRight,
  Leaf,
  Search,
  Utensils,
} from 'lucide-react'

import type { FoodCategory, FoodItem } from '../../../../../../types/food'
import {
  CATEGORY_LABELS,
  FOOD_CATEGORIES,
} from '../../../../../../constants/food'
import { foodService } from '../../../../../../services/foodService'
import { foodDetailsPath } from '../../../../../../routes/routePaths'
import { FoodImage } from '../../../../../../components/common/FoodImage'
import { PageState } from '../../../../../../components/ui/PageState'
import { Button } from '../../../../../../components/ui/Button'
import { INPUT_CLASSES } from '../../../../../../components/ui/Field'

type CategoryFilter = FoodCategory | 'ALL'
type SortOption = 'name' | 'calories-asc' | 'calories-desc' | 'protein-desc'

const SORT_OPTIONS: ReadonlyArray<{ value: SortOption; label: string }> = [
  { value: 'name', label: 'Name (A–Z)' },
  { value: 'calories-asc', label: 'Calories: low to high' },
  { value: 'calories-desc', label: 'Calories: high to low' },
  { value: 'protein-desc', label: 'Protein: high to low' },
]

const PAGE_SIZE = 8

function CategoryChip({
  active,
  onClick,
  children,
}: {
  active: boolean
  onClick: () => void
  children: string
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={active}
      className={`shrink-0 cursor-pointer rounded-full border px-3.5 py-1.5 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${
        active
          ? 'border-primary bg-primary text-primary-foreground'
          : 'border-border bg-background text-muted-foreground hover:border-primary/40 hover:text-foreground'
      }`}
    >
      {children}
    </button>
  )
}

function FoodGridSkeleton() {
  return (
    <div
      aria-hidden="true"
      className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
    >
      {Array.from({ length: PAGE_SIZE }).map((_, index) => (
        <div
          key={index}
          className="animate-pulse overflow-hidden rounded-xl border border-border bg-card"
        >
          <div className="aspect-[4/3] bg-muted" />
          <div className="space-y-2 p-4">
            <div className="h-3 w-16 rounded bg-muted" />
            <div className="h-4 w-3/4 rounded bg-muted" />
            <div className="h-3 w-full rounded bg-muted" />
            <div className="h-3 w-2/3 rounded bg-muted" />
          </div>
        </div>
      ))}
    </div>
  )
}

/**
 * Food catalog.
 *
 * Search (server-side, debounced) and category (server-side) can be
 * combined; results are sorted and paginated on the client because the
 * backend does not expose sort/page params.
 */
export function FoodsPage() {
  const [foods, setFoods] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')
  const [category, setCategory] = useState<CategoryFilter>('ALL')
  const [sort, setSort] = useState<SortOption>('name')
  const [page, setPage] = useState(1)
  const [reloadKey, setReloadKey] = useState(0)

  // Debounce the search input so we don't hit the API per keystroke.
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query.trim()), 300)
    return () => clearTimeout(timer)
  }, [query])

  // Load foods whenever the search term, category, or retry changes.
  useEffect(() => {
    let cancelled = false

    const load = async () => {
      setIsLoading(true)
      setError(null)

      try {
        let result: FoodItem[]

        if (debouncedQuery) {
          result = await foodService.searchFoods(debouncedQuery)
          if (category !== 'ALL') {
            result = result.filter((food) => food.category === category)
          }
        } else if (category !== 'ALL') {
          result = await foodService.getFoodsByCategory(category)
        } else {
          result = await foodService.getAllFoods()
        }

        if (!cancelled) {
          setFoods(result)
          setPage(1)
        }
      } catch (err) {
        if (!cancelled) setError(getErrorMessage(err))
      } finally {
        if (!cancelled) setIsLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [debouncedQuery, category, reloadKey])

  // Reset to the first page when the sort order changes.
  useEffect(() => {
    setPage(1)
  }, [sort])

  const sortedFoods = useMemo(() => {
    const sorted = [...foods]

    switch (sort) {
      case 'name':
        sorted.sort((a, b) => a.name.localeCompare(b.name))
        break
      case 'calories-asc':
        sorted.sort((a, b) => a.calories - b.calories)
        break
      case 'calories-desc':
        sorted.sort((a, b) => b.calories - a.calories)
        break
      case 'protein-desc':
        sorted.sort((a, b) => b.protein - a.protein)
        break
    }

    return sorted
  }, [foods, sort])

  const totalPages = Math.max(1, Math.ceil(sortedFoods.length / PAGE_SIZE))
  const pagedFoods = sortedFoods.slice(
    (page - 1) * PAGE_SIZE,
    page * PAGE_SIZE,
  )

  const clearFilters = () => {
    setQuery('')
    setCategory('ALL')
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="h-8 w-48 rounded-lg bg-muted" />
        <div className="mt-2 h-4 w-72 max-w-full rounded bg-muted" />
        <div className="mt-8">
          <FoodGridSkeleton />
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <PageState
        icon={Utensils}
        title="Couldn't load foods"
        message={error}
        action={
          <Button onClick={() => setReloadKey((key) => key + 1)}>Try again</Button>
        }
      />
    )
  }

  if (sortedFoods.length === 0) {
    return (
      <PageState
        icon={Search}
        title="No foods found"
        message="Try a different search term or category."
        action={
          <Button variant="outline" onClick={clearFilters}>
            Clear filters
          </Button>
        }
      />
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      {/* Header */}
      <header>
        <h1 className="text-3xl font-bold tracking-tight">Food catalog</h1>
        <p className="mt-1 text-muted-foreground">
          Discover foods by category or search for something specific.
        </p>
      </header>

      {/* Toolbar */}
      <div className="mt-8 space-y-4">
        <div className="relative max-w-md">
          <Search
            aria-hidden="true"
            className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
          />
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search foods…"
            aria-label="Search foods"
            className={`${INPUT_CLASSES} pl-9`}
          />
        </div>

        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div
            role="group"
            aria-label="Filter by category"
            className="flex gap-2 overflow-x-auto pb-1"
          >
            <CategoryChip
              active={category === 'ALL'}
              onClick={() => setCategory('ALL')}
            >
              All
            </CategoryChip>
            {FOOD_CATEGORIES.map((cat) => (
              <CategoryChip
                key={cat}
                active={category === cat}
                onClick={() => setCategory(cat)}
              >
                {CATEGORY_LABELS[cat]}
              </CategoryChip>
            ))}
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="sort" className="text-sm text-muted-foreground">
              Sort by
            </label>
            <select
              id="sort"
              value={sort}
              onChange={(event) => setSort(event.target.value as SortOption)}
              className={`${INPUT_CLASSES} w-full cursor-pointer sm:w-56`}
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Grid */}
      <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {pagedFoods.map((food) => (
          <Link
            key={food.foodId}
            to={foodDetailsPath(food.foodId)}
            className="group overflow-hidden rounded-xl border border-border bg-card shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          >
            <div className="relative">
              <FoodImage
                src={food.imageUrl}
                alt={food.name}
                className="aspect-[4/3] rounded-none"
              />
              {food.vegetarian ? (
                <span className="absolute left-2.5 top-2.5 inline-flex items-center gap-1 rounded-full bg-background/90 px-2 py-0.5 text-[11px] font-medium text-foreground backdrop-blur">
                  <Leaf aria-hidden="true" className="h-3 w-3 text-primary" />
                  Vegetarian
                </span>
              ) : null}
            </div>

            <div className="p-4">
              <p className="text-xs font-medium uppercase tracking-wide text-primary">
                {CATEGORY_LABELS[food.category]}
              </p>
              <h3 className="mt-1 font-semibold text-foreground transition-colors group-hover:text-primary">
                {food.name}
              </h3>
              <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                {food.description}
              </p>

              <dl className="mt-4 grid grid-cols-4 gap-2 border-t border-border pt-3 text-center">
                <div>
                  <dt className="text-[10px] uppercase tracking-wide text-muted-foreground">
                    Cal
                  </dt>
                  <dd className="mt-0.5 text-sm font-semibold text-foreground">
                    {food.calories}
                  </dd>
                </div>
                <div>
                  <dt className="text-[10px] uppercase tracking-wide text-muted-foreground">
                    Protein
                  </dt>
                  <dd className="mt-0.5 text-sm font-semibold text-foreground">
                    {food.protein}g
                  </dd>
                </div>
                <div>
                  <dt className="text-[10px] uppercase tracking-wide text-muted-foreground">
                    Carbs
                  </dt>
                  <dd className="mt-0.5 text-sm font-semibold text-foreground">
                    {food.carbohydrates}g
                  </dd>
                </div>
                <div>
                  <dt className="text-[10px] uppercase tracking-wide text-muted-foreground">
                    Fat
                  </dt>
                  <dd className="mt-0.5 text-sm font-semibold text-foreground">
                    {food.fat}g
                  </dd>
                </div>
              </dl>
            </div>
          </Link>
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 ? (
        <nav
          aria-label="Pagination"
          className="mt-10 flex items-center justify-center gap-4"
        >
          <Button
            variant="outline"
            size="sm"
            disabled={page <= 1}
            onClick={() => setPage((current) => Math.max(1, current - 1))}
          >
            <ChevronLeft aria-hidden="true" className="h-4 w-4" />
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {page} of {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= totalPages}
            onClick={() =>
              setPage((current) => Math.min(totalPages, current + 1))
            }
          >
            Next
            <ChevronRight aria-hidden="true" className="h-4 w-4" />
          </Button>
        </nav>
      ) : null}
    </div>
  )
}

/**
 * Extracts a human-readable message from an API/network error.
 * Spring Boot error bodies usually carry { message: "..." }.
 */
function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string } | undefined
    if (data?.message) return data.message
    return error.message
  }
  if (error instanceof Error && error.message) return error.message
  return 'Something went wrong. Please try again.'
}