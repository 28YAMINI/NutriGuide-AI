import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'

import { ArrowLeft, Leaf } from 'lucide-react'
import type { FoodItem } from '../../../../../../../types/food'
import { foodService } from '../../../../../../../services/foodService'
import { routePaths } from '../../../../../../../routes/routePaths'
import { PageState } from '../../../../../../../components/ui/PageState'
import { buttonClassName } from '../../../../../../../components/ui/Button'
import { formatCalories, formatMacro } from '../../../../../../../utils/format'
import { CATEGORY_LABELS } from '../../../../../../../constants/food'
import { getErrorMessage } from '../../../../../../../utils/error'


function FoodDetailsSkeleton() {
  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <div className="h-4 w-24 animate-pulse rounded bg-muted" />
      <div className="mt-6 grid gap-8 lg:grid-cols-[1fr_1.4fr]">
        <div className="h-64 animate-pulse rounded-xl border border-border bg-card lg:h-96" />
        <div className="space-y-4">
          <div className="h-6 w-2/3 animate-pulse rounded bg-muted" />
          <div className="h-4 w-1/3 animate-pulse rounded bg-muted" />
          <div className="h-4 w-full animate-pulse rounded bg-muted" />
          <div className="grid grid-cols-2 gap-3 pt-4 sm:grid-cols-3">
            {[0, 1, 2, 3, 4, 5].map((i) => (
              <div
                key={i}
                className="h-20 animate-pulse rounded-xl border border-border bg-card"
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

/** Single food detail — nutrition breakdown for one catalog item. */
export function FoodDetailsPage() {
  const { id } = useParams<{ id: string }>()
  const [food, setFood] = useState<FoodItem | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      if (!id) throw new Error('Food id is missing.')
      setFood(await foodService.getFoodById(Number(id)))
    } catch (err) {
      setError(getErrorMessage(err))
      setFood(null)
    } finally {
      setIsLoading(false)
    }
  }, [id])

  useEffect(() => {
    void load()
  }, [load])

  if (isLoading) {
    return <FoodDetailsSkeleton />
  }

  if (error || !food) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
        <PageState
          icon={Leaf}
          title="Food not found"
          message={error ?? 'This food may have been removed.'}
          action={
            <Link to={routePaths.foods} className={buttonClassName('primary', 'md')}>
              Back to food catalog
            </Link>
          }
        />
      </div>
    )
  }

  const nutrition = [
    { label: 'Calories', value: formatCalories(food.calories) },
    { label: 'Protein', value: `${formatMacro(food.protein)} g` },
    { label: 'Carbohydrates', value: `${formatMacro(food.carbohydrates)} g` },
    { label: 'Fat', value: `${formatMacro(food.fat)} g` },
    { label: 'Fiber', value: `${formatMacro(food.fiber)} g` },
  ]

  return (
    <main className="mx-auto max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <Link
        to={routePaths.foods}
        className="inline-flex items-center gap-1.5 rounded-md text-sm font-medium text-muted-foreground transition-colors hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
      >
        <ArrowLeft aria-hidden="true" className="h-4 w-4" />
        Back to foods
      </Link>

      <div className="mt-6 grid gap-8 lg:grid-cols-[1fr_1.4fr]">
        {/* Image */}
        <div className="overflow-hidden rounded-xl border border-border bg-card">
          {food.imageUrl ? (
            <img
              src={food.imageUrl}
              alt={food.name}
              className="h-64 w-full object-cover lg:h-full"
            />
          ) : (
            <div className="flex h-64 w-full items-center justify-center bg-primary/10 text-primary lg:h-full">
              <Leaf aria-hidden="true" className="h-12 w-12" />
            </div>
          )}
        </div>

        {/* Info */}
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded-full bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
              {CATEGORY_LABELS[food.category]}
            </span>
            {food.vegetarian ? (
              <span className="rounded-full bg-emerald-600/10 px-2.5 py-1 text-xs font-medium text-emerald-600 dark:text-emerald-400">
                Vegetarian
              </span>
            ) : null}
          </div>

          <h1 className="mt-3 text-3xl font-bold tracking-tight">{food.name}</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Serving size:{' '}
            <span className="font-medium text-foreground">{food.servingSize}</span>
          </p>
          <p className="mt-4 text-muted-foreground">{food.description}</p>

          <div className="mt-8">
            <h2 className="text-sm font-semibold text-foreground">
              Nutrition per serving
            </h2>
            <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
              {nutrition.map((item) => (
                <div
                  key={item.label}
                  className="rounded-xl border border-border bg-card p-4"
                >
                  <dt className="text-xs text-muted-foreground">{item.label}</dt>
                  <dd className="mt-1 text-lg font-bold tracking-tight text-foreground">
                    {item.value}
                  </dd>
                </div>
              ))}
            </dl>
          </div>
        </div>
      </div>
    </main>
  )
}

