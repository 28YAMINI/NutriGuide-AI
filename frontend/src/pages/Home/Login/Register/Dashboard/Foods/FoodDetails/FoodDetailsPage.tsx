import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import axios from 'axios'

import { ChevronLeft, Leaf, Utensils } from 'lucide-react'

import type { FoodItem } from '../../../../../../../types/food'
import { CATEGORY_LABELS } from '../../../../../../../constants/food'
import { foodService } from '../../../../../../../services/foodService'
import {
  foodDetailsPath,
  routePaths,
} from '../../../../../../../routes/routePaths'
import { FoodImage } from '../../../../../../../components/common/FoodImage'
import { PageState } from '../../../../../../../components/ui/PageState'
import { buttonClassName } from '../../../../../../../components/ui/Button'

function NutritionTile({
  label,
  value,
  unit,
  highlighted = false,
}: {
  label: string
  value: string
  unit: string
  highlighted?: boolean
}) {
  return (
    <div
      className={
        highlighted
          ? 'rounded-xl border border-primary/30 bg-primary/10 p-4'
          : 'rounded-xl border border-border bg-card p-4'
      }
    >
      <dt className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
        {label}
      </dt>
      <dd
        className={`mt-1 text-2xl font-bold tracking-tight ${
          highlighted ? 'text-primary' : 'text-foreground'
        }`}
      >
        {value}
        <span className="ml-1 text-sm font-medium text-muted-foreground">
          {unit}
        </span>
      </dd>
    </div>
  )
}

function FoodDetailsSkeleton() {
  return (
    <div
      aria-hidden="true"
      className="mx-auto max-w-7xl animate-pulse px-4 py-10 sm:px-6 lg:px-8"
    >
      <div className="grid gap-8 lg:grid-cols-2">
        <div className="aspect-[4/3] rounded-2xl bg-muted" />
        <div className="space-y-4">
          <div className="h-4 w-24 rounded bg-muted" />
          <div className="h-9 w-2/3 rounded bg-muted" />
          <div className="h-4 w-full rounded bg-muted" />
          <div className="h-4 w-4/5 rounded bg-muted" />
          <div className="grid grid-cols-2 gap-4 pt-4 sm:grid-cols-3">
            {[0, 1, 2, 3, 4].map((index) => (
              <div key={index} className="h-28 rounded-xl bg-muted" />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

/**
 * Food detail page.
 *
 * Loads a single food by id, shows its nutrition facts, and surfaces
 * up to four related foods from the same category. The related list is
 * optional — if that request fails, the main content still renders.
 */
export function FoodDetailsPage() {
  const { id } = useParams()
  const [food, setFood] = useState<FoodItem | null>(null)
  const [related, setRelated] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    let cancelled = false

    const load = async () => {
      setIsLoading(true)
      setError(null)

      try {
        const result = await foodService.getFoodById(id)
        if (!cancelled) setFood(result)
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
  }, [id])

  // Related foods from the same category (best-effort).
  useEffect(() => {
    if (!food) return
    let cancelled = false

    foodService
      .getFoodsByCategory(food.category)
      .then((result) => {
        if (!cancelled) {
          setRelated(
            result.filter((item) => item.foodId !== food.foodId).slice(0, 4),
          )
        }
      })
      .catch(() => {
        // Related foods are optional — never block the main content.
      })

    return () => {
      cancelled = true
    }
  }, [food])

  if (isLoading) {
    return <FoodDetailsSkeleton />
  }

  if (error || !food) {
    return (
      <PageState
        icon={Utensils}
        title={error ? "Couldn't load this food" : 'Food not found'}
        message={
          error ?? 'This food may have been removed from the catalog.'
        }
        action={
          <Link
            to={routePaths.foods}
            className={buttonClassName('primary', 'md')}
          >
            Browse foods
          </Link>
        }
      />
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <Link
        to={routePaths.foods}
        className="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm"
      >
        <ChevronLeft aria-hidden="true" className="h-4 w-4" />
        Back to foods
      </Link>

      <div className="mt-6 grid gap-8 lg:grid-cols-2 lg:items-start">
        {/* Image */}
        <FoodImage
          src={food.imageUrl}
          alt={food.name}
          loading="eager"
          className="aspect-[4/3] rounded-2xl shadow-sm"
        />

        {/* Details */}
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs font-semibold uppercase tracking-wide text-primary">
              {CATEGORY_LABELS[food.category]}
            </span>
            {food.vegetarian ? (
              <span className="inline-flex items-center gap-1 rounded-full border border-primary/30 bg-primary/10 px-2 py-0.5 text-[11px] font-medium text-primary">
                <Leaf aria-hidden="true" className="h-3 w-3" />
                Vegetarian
              </span>
            ) : null}
          </div>

          <h1 className="mt-2 text-3xl font-bold tracking-tight sm:text-4xl">
            {food.name}
          </h1>

          <p className="mt-3 text-base leading-relaxed text-muted-foreground">
            {food.description}
          </p>

          <p className="mt-3 text-sm text-muted-foreground">
            Serving size:{' '}
            <span className="font-medium text-foreground">
              {food.servingSize}
            </span>
          </p>

          {/* Nutrition facts */}
          <dl className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3">
            <NutritionTile
              label="Calories"
              value={String(food.calories)}
              unit="kcal"
              highlighted
            />
            <NutritionTile label="Protein" value={String(food.protein)} unit="g" />
            <NutritionTile
              label="Carbs"
              value={String(food.carbohydrates)}
              unit="g"
            />
            <NutritionTile label="Fat" value={String(food.fat)} unit="g" />
            <NutritionTile label="Fiber" value={String(food.fiber)} unit="g" />
          </dl>
        </div>
      </div>

      {/* More in this category */}
      {related.length > 0 ? (
        <section aria-labelledby="related-heading" className="mt-16">
          <h2
            id="related-heading"
            className="text-xl font-semibold tracking-tight"
          >
            More in {CATEGORY_LABELS[food.category]}
          </h2>
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {related.map((item) => (
              <Link
                key={item.foodId}
                to={foodDetailsPath(item.foodId)}
                className="group overflow-hidden rounded-xl border border-border bg-card shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                <FoodImage
                  src={item.imageUrl}
                  alt={item.name}
                  className="aspect-[4/3] rounded-none"
                />
                <div className="p-3.5">
                  <h3 className="truncate text-sm font-semibold text-foreground transition-colors group-hover:text-primary">
                    {item.name}
                  </h3>
                  <p className="mt-0.5 text-xs text-muted-foreground">
                    {item.calories} kcal · {CATEGORY_LABELS[item.category]}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        </section>
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