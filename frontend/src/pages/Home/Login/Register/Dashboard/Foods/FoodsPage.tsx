import { useCallback, useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { CATEGORY_LABELS } from '../../../../../../constants/food'
import type { FoodCategory, FoodItem } from '../../../../../../types/food'
import { foodDetailsPath } from '../../../../../../routes/routePaths'
import { Card } from '../../../../../../components/ui/Card'
import { Leaf, Search, SearchX, UtensilsCrossed } from 'lucide-react'
import { formatMacro } from '../../../../../../utils/format'
import { foodService } from '../../../../../../services/foodService'
import { INPUT_CLASSES } from '../../../../../../components/ui/Field'
import { Button } from '../../../../../../components/ui/Button'
import { PageState } from '../../../../../../components/ui/PageState'
import { getErrorMessage } from '../../../../../../utils/error'


const CATEGORIES = Object.keys(CATEGORY_LABELS) as FoodCategory[]

const PAGE_SIZE = 9

type SortKey = 'name' | 'calories-asc' | 'calories-desc'
type CategoryFilter = FoodCategory | 'ALL'

const SORT_OPTIONS: ReadonlyArray<{ value: SortKey; label: string }> = [
  { value: 'name', label: 'Name A–Z' },
  { value: 'calories-asc', label: 'Calories (low → high)' },
  { value: 'calories-desc', label: 'Calories (high → low)' },
]

function sortFoods(foods: FoodItem[], sort: SortKey): FoodItem[] {
  switch (sort) {
    case 'name':
      return [...foods].sort((a, b) => a.name.localeCompare(b.name))
    case 'calories-asc':
      return [...foods].sort((a, b) => a.calories - b.calories)
    case 'calories-desc':
      return [...foods].sort((a, b) => b.calories - a.calories)
    default:
      return [...foods]
  }
}

interface CategoryChipProps {
  active: boolean
  label: string
  onClick: () => void
}

/** Pill filter for food categories. aria-pressed reflects the active state. */
function CategoryChip({ active, label, onClick }: CategoryChipProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={active}
      className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary ${
        active
          ? 'bg-primary text-primary-foreground'
          : 'bg-muted text-muted-foreground hover:bg-muted/70 hover:text-foreground'
      }`}
    >
      {label}
    </button>
  )
}

interface FoodCardProps {
  food: FoodItem
}

/** One catalog card; the whole card links to the detail page. */
function FoodCard({ food }: FoodCardProps) {
  return (
    <Link
      to={foodDetailsPath(food.id)}
      className="group block rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
    >
      <Card className="h-full overflow-hidden transition-colors group-hover:border-primary/50">
        <div className="relative h-36 bg-muted">
          {food.imageUrl ? (
            <img
              src={food.imageUrl}
              alt={food.name}
              loading="lazy"
              className="h-full w-full object-cover"
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-primary/10 text-primary">
              <Leaf aria-hidden="true" className="h-8 w-8" />
            </div>
          )}
          {food.vegetarian ? (
            <span className="absolute left-3 top-3 rounded-full bg-emerald-600 px-2 py-0.5 text-[11px] font-medium text-white">
              Vegetarian
            </span>
          ) : null}
        </div>

        <div className="flex flex-col p-4">
          <h3 className="text-sm font-semibold text-foreground">{food.name}</h3>
          <p className="mt-0.5 text-xs font-medium text-primary">
            {CATEGORY_LABELS[food.category]}
          </p>
          <p className="mt-2 line-clamp-2 text-sm text-muted-foreground">
            {food.description}
          </p>

          <dl className="mt-4 grid grid-cols-4 gap-2 border-t border-border pt-3 text-center">
            <div>
              <dt className="text-[11px] text-muted-foreground">Cal</dt>
              <dd className="mt-0.5 text-xs font-semibold text-foreground">
                {Math.round(food.calories)}
              </dd>
            </div>
            <div>
              <dt className="text-[11px] text-muted-foreground">Protein</dt>
              <dd className="mt-0.5 text-xs font-semibold text-foreground">
                {formatMacro(food.protein)}g
              </dd>
            </div>
            <div>
              <dt className="text-[11px] text-muted-foreground">Carbs</dt>
              <dd className="mt-0.5 text-xs font-semibold text-foreground">
                {formatMacro(food.carbohydrates)}g
              </dd>
            </div>
            <div>
              <dt className="text-[11px] text-muted-foreground">Fat</dt>
              <dd className="mt-0.5 text-xs font-semibold text-foreground">
                {formatMacro(food.fat)}g
              </dd>
            </div>
          </dl>
        </div>
      </Card>
    </Link>
  )
}

function FoodsSkeleton() {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {[0, 1, 2, 3, 4, 5].map((i) => (
        <div
          key={i}
          className="h-72 animate-pulse rounded-xl border border-border bg-card"
        />
      ))}
    </div>
  )
}

/** Public food catalog — search, category filter, sort and pagination. */
export function FoodsPage() {
  const [foods, setFoods] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [category, setCategory] = useState<CategoryFilter>('ALL')
  const [searchInput, setSearchInput] = useState('')
  const [searchTerm, setSearchTerm] = useState('')

  const [sort, setSort] = useState<SortKey>('name')
  const [page, setPage] = useState(1)

  const load = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const term = searchTerm.trim()
      let result: FoodItem[]
      if (term) {
        result = await foodService.searchFoods(term)
      } else if (category !== 'ALL') {
        result = await foodService.getFoodsByCategory(category)
      } else {
        result = await foodService.getAllFoods()
      }
      setFoods(result)
      setPage(1)
    } catch (err) {
      setError(getErrorMessage(err))
      setFoods([])
    } finally {
      setIsLoading(false)
    }
  }, [searchTerm, category])

  useEffect(() => {
    void load()
  }, [load])

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setSearchTerm(searchInput.trim())
  }

  const selectCategory = (next: CategoryFilter) => {
    setCategory((prev) => (prev === next ? 'ALL' : next))
    setSearchTerm('')
    setSearchInput('')
  }

  const clearFilters = () => {
    setCategory('ALL')
    setSearchTerm('')
    setSearchInput('')
  }

  const sortedFoods = useMemo(() => sortFoods(foods, sort), [foods, sort])
  const pageCount = Math.max(1, Math.ceil(sortedFoods.length / PAGE_SIZE))
  const currentPage = Math.min(page, pageCount)
  const visibleFoods = sortedFoods.slice(
    (currentPage - 1) * PAGE_SIZE,
    currentPage * PAGE_SIZE,
  )
  const hasFilters = category !== 'ALL' || searchTerm !== ''
  const from = sortedFoods.length === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1
  const to = Math.min(currentPage * PAGE_SIZE, sortedFoods.length)

  return (
    <main className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <header>
        <h1 className="text-3xl font-bold tracking-tight">Food catalog</h1>
        <p className="mt-1 max-w-2xl text-muted-foreground">
          Explore the nutrition database — search by name or browse by category.
        </p>
      </header>

      {/* Search + filters */}
      <div className="mt-8 flex flex-col gap-4">
        <form onSubmit={handleSearch} role="search" className="flex gap-2">
          <div className="relative flex-1">
            <Search
              aria-hidden="true"
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
            />
            <input
              type="search"
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Search foods…"
              aria-label="Search foods"
              className={`${INPUT_CLASSES} pl-9`}
            />
          </div>
          <Button type="submit" isLoading={isLoading}>
            Search
          </Button>
        </form>

        <div className="flex flex-col justify-between gap-3 lg:flex-row lg:items-center">
          <div
            className="flex flex-wrap gap-2"
            role="group"
            aria-label="Filter by category"
          >
            <CategoryChip
              active={category === 'ALL'}
              label="All"
              onClick={() => selectCategory('ALL')}
            />
            {CATEGORIES.map((cat) => (
              <CategoryChip
                key={cat}
                active={category === cat}
                label={CATEGORY_LABELS[cat]}
                onClick={() => selectCategory(cat)}
              />
            ))}
          </div>

          <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
            <span>Sort by</span>
            <div className="w-52">
              <select
                value={sort}
                onChange={(event) => setSort(event.target.value as SortKey)}
                aria-label="Sort foods"
                className={INPUT_CLASSES}
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
      </div>

      {/* Content */}
      <div className="mt-8">
        {isLoading ? (
          <FoodsSkeleton />
        ) : error ? (
          <PageState
            icon={SearchX}
            title="Couldn't load foods"
            message={error}
            action={
              <Button variant="outline" onClick={() => void load()}>
                Try again
              </Button>
            }
          />
        ) : foods.length === 0 ? (
          <PageState
            icon={UtensilsCrossed}
            title="No foods found"
            message={
              searchTerm
                ? `No results for “${searchTerm}”. Try a different search or category.`
                : 'Nothing in this category yet. Check back soon.'
            }
            action={
              hasFilters ? (
                <Button variant="outline" onClick={clearFilters}>
                  Clear filters
                </Button>
              ) : undefined
            }
          />
        ) : (
          <>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {visibleFoods.map((food) => (
                <FoodCard key={food.id} food={food} />
              ))}
            </div>

            {pageCount > 1 ? (
              <div className="mt-8 flex flex-col items-center justify-between gap-3 border-t border-border pt-6 sm:flex-row">
                <p className="text-sm text-muted-foreground">
                  Showing {from}–{to} of {sortedFoods.length} foods
                </p>
                <div className="flex items-center gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={currentPage === 1}
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                  >
                    Previous
                  </Button>
                  <span className="text-sm text-muted-foreground" aria-live="polite">
                    Page {currentPage} of {pageCount}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={currentPage === pageCount}
                    onClick={() => setPage((p) => Math.min(pageCount, p + 1))}
                  >
                    Next
                  </Button>
                </div>
              </div>
            ) : null}
          </>
        )}
      </div>
    </main>
  )
}


