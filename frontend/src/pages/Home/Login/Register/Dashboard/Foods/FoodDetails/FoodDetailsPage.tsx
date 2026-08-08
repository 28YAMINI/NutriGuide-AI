import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { ArrowLeft, Leaf, UtensilsCrossed } from 'lucide-react'

import { routePaths } from '../../../../../../../routes/routePaths'
import type { FoodItem } from '../../../../../../../types/food'
import { CATEGORY_LABELS } from '../../../../../../../constants/food'
import { foodService } from '../../../../../../../services/foodService'

function formatMacro(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return 'Could not load this food. Please try again.'
}

interface NutritionRowProps {
  label: string
  value: string
}

function NutritionRow({ label, value }: NutritionRowProps) {
  return (
    <div className="flex items-center justify-between py-2.5">
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="text-sm font-semibold">{value}</dd>
    </div>
  )
}

interface MacroSplitBarProps {
  protein: number
  carbohydrates: number
  fat: number
}

/** Proportional protein/carbs/fat bar — a quick visual calorie source split. */
function MacroSplitBar({ protein, carbohydrates, fat }: MacroSplitBarProps) {
  const total = protein + carbohydrates + fat

  if (total <= 0) {
    return null
  }

  const width = (value: number) => `${(value / total) * 100}%`

  return (
    <div className="mt-5 border-t border-border pt-4">
      <div
        role="img"
        aria-label={`Macro split — ${formatMacro(protein)}g protein, ${formatMacro(carbohydrates)}g carbohydrates, ${formatMacro(fat)}g fat`}
        className="flex h-2.5 w-full overflow-hidden rounded-full bg-muted"
      >
        <span className="bg-primary" style={{ width: width(protein) }} />
        <span className="bg-amber-500" style={{ width: width(carbohydrates) }} />
        <span className="bg-emerald-500" style={{ width: width(fat) }} />
      </div>
      <div className="mt-2.5 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
        <span className="inline-flex items-center gap-1.5">
          <span aria-hidden="true" className="h-2 w-2 rounded-full bg-primary" />
          Protein {formatMacro(protein)}g
        </span>
        <span className="inline-flex items-center gap-1.5">
          <span aria-hidden="true" className="h-2 w-2 rounded-full bg-amber-500" />
          Carbs {formatMacro(carbohydrates)}g
        </span>
        <span className="inline-flex items-center gap-1.5">
          <span aria-hidden="true" className="h-2 w-2 rounded-full bg-emerald-500" />
          Fat {formatMacro(fat)}g
        </span>
      </div>
    </div>
  )
}

function DetailsSkeleton() {
  return (
    <div className="mt-8 grid animate-pulse gap-8 lg:grid-cols-2">
      <div className="aspect-[4/3] rounded-2xl bg-muted" />
      <div className="space-y-4">
        <div className="h-5 w-24 rounded-full bg-muted" />
        <div className="h-8 w-2/3 rounded bg-muted" />
        <div className="h-4 w-full rounded bg-muted" />
        <div className="h-4 w-4/5 rounded bg-muted" />
        <div className="mt-6 space-y-3 rounded-xl border border-border bg-card p-5">
          <div className="h-4 w-32 rounded bg-muted" />
          <div className="h-4 w-full rounded bg-muted" />
          <div className="h-4 w-full rounded bg-muted" />
          <div className="h-4 w-3/4 rounded bg-muted" />
        </div>
      </div>
    </div>
  )
}

/**
 * Single-food detail view at /foods/:id.
 * Public route — fetches via GET /foods/{id}.
 */
export function FoodDetailsPage() {
  const { id } = useParams<{ id: string }>()

  const [food, setFood] = useState<FoodItem | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (!id) {
      return
    }

    let ignore = false

    const load = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const result = await foodService.getFoodById(id)
        if (!ignore) {
          setFood(result)
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
  }, [id, reloadKey])

  if (!id) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="mt-8 flex flex-col items-center justify-center rounded-xl border border-border px-6 py-16 text-center">
          <h1 className="text-lg font-semibold">Food not found</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            This food item doesn't exist.
          </p>
          <Link
            to={routePaths.foods}
            className="mt-5 inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Browse foods
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6 lg:px-8">
      <Link
        to={routePaths.foods}
        className="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft aria-hidden="true" className="h-4 w-4" />
        Back to foods
      </Link>

      {/* Loading */}
      {isLoading && (
        <>
          <p role="status" className="sr-only">
            Loading food…
          </p>
          <DetailsSkeleton />
        </>
      )}

      {/* Error */}
      {!isLoading && error && (
        <div className="mt-8 flex flex-col items-center justify-center rounded-xl border border-border px-6 py-16 text-center">
          <h1 className="text-lg font-semibold">Couldn't load this food</h1>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">{error}</p>
          <div className="mt-5 flex gap-3">
            <button
              type="button"
              onClick={() => setReloadKey((key) => key + 1)}
              className="inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
            >
              Try again
            </button>
            <Link
              to={routePaths.foods}
              className="inline-flex items-center justify-center rounded-lg border border-border bg-background px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-muted"
            >
              Browse foods
            </Link>
          </div>
        </div>
      )}

      {/* Content */}
      {!isLoading && !error && food && (
        <div className="mt-8 grid gap-8 lg:grid-cols-2 lg:items-start">
          {/* Image */}
          <div className="aspect-[4/3] overflow-hidden rounded-2xl border border-border bg-muted">
            {food.imageUrl ? (
              <img
                src={food.imageUrl}
                alt={food.name}
                className="h-full w-full object-cover"
              />
            ) : (
              <div className="flex h-full w-full items-center justify-center text-muted-foreground">
                <UtensilsCrossed aria-hidden="true" className="h-12 w-12" />
              </div>
            )}
          </div>

          {/* Info */}
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
                {CATEGORY_LABELS[food.category]}
              </span>
              {food.vegetarian && (
                <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-2.5 py-0.5 text-xs font-medium text-emerald-600 dark:text-emerald-400">
                  <Leaf aria-hidden="true" className="h-3 w-3" />
                  Vegetarian
                </span>
              )}
            </div>

            <h1 className="mt-3 text-3xl font-bold tracking-tight">{food.name}</h1>

            {food.description && (
              <p className="mt-3 text-muted-foreground">{food.description}</p>
            )}

            {/* Nutrition facts */}
            <div className="mt-6 rounded-xl border border-border bg-card p-5 sm:p-6">
              <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                Nutrition facts
              </h2>
              <p className="mt-1 text-xs text-muted-foreground">
                Per serving ({food.servingSize})
              </p>

              <dl className="mt-3 divide-y divide-border">
                <NutritionRow label="Calories" value={`${Math.round(food.calories)} kcal`} />
                <NutritionRow label="Protein" value={`${formatMacro(food.protein)} g`} />
                <NutritionRow label="Carbohydrates" value={`${formatMacro(food.carbohydrates)} g`} />
                <NutritionRow label="Fat" value={`${formatMacro(food.fat)} g`} />
                <NutritionRow label="Fiber" value={`${formatMacro(food.fiber)} g`} />
              </dl>

              <MacroSplitBar
                protein={food.protein}
                carbohydrates={food.carbohydrates}
                fat={food.fat}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  )
}