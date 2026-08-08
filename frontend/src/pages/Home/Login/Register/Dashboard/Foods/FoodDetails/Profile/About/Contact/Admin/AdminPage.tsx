import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { useForm } from 'react-hook-form'

import { Pencil, Plus, Trash2, UtensilsCrossed, X } from 'lucide-react'
import { CATEGORY_LABELS } from '../../../../../../../../../../../constants/food'
import type { CreateFoodRequest, FoodCategory, FoodItem } from '../../../../../../../../../../../types/food'
import { foodService } from '../../../../../../../../../../../services/foodService'
import { Spinner } from '../../../../../../../../../../../components/common/Spinner'



const INPUT_CLASSES =
  'w-full rounded-lg border border-border bg-background px-3.5 py-2.5 text-sm text-foreground placeholder:text-muted-foreground transition-colors focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:cursor-not-allowed disabled:opacity-60'

const CATEGORY_OPTIONS = Object.entries(CATEGORY_LABELS).map(
  ([value, label]) => ({
    value: value as FoodCategory,
    label,
  }),
)

function formatMacro(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return 'Something went wrong. Please try again.'
}

/* ---------- form ---------- */

interface FoodFormValues {
  name: string
  description: string
  category: FoodCategory | ''
  calories: string
  protein: string
  carbohydrates: string
  fat: string
  fiber: string
  servingSize: string
  imageUrl: string
  vegetarian: boolean
}

const EMPTY_FORM: FoodFormValues = {
  name: '',
  description: '',
  category: '',
  calories: '',
  protein: '',
  carbohydrates: '',
  fat: '',
  fiber: '',
  servingSize: '',
  imageUrl: '',
  vegetarian: false,
}

function toFormValues(food: FoodItem): FoodFormValues {
  return {
    name: food.name,
    description: food.description,
    category: food.category,
    calories: String(food.calories),
    protein: String(food.protein),
    carbohydrates: String(food.carbohydrates),
    fat: String(food.fat),
    fiber: String(food.fiber),
    servingSize: food.servingSize,
    imageUrl: food.imageUrl ?? '',
    vegetarian: food.vegetarian,
  }
}

function toPayload(values: FoodFormValues): CreateFoodRequest {
  return {
    name: values.name.trim(),
    description: values.description.trim(),
    category: values.category as FoodCategory,
    calories: Number(values.calories),
    protein: Number(values.protein),
    carbohydrates: Number(values.carbohydrates),
    fat: Number(values.fat),
    fiber: Number(values.fiber),
    servingSize: values.servingSize.trim(),
    imageUrl: values.imageUrl.trim() || undefined,
    vegetarian: values.vegetarian,
  }
}

interface FieldProps {
  label: string
  htmlFor: string
  error?: string
  children: ReactNode
}

function Field({ label, htmlFor, error, children }: FieldProps) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={htmlFor} className="block text-sm font-medium text-foreground">
        {label}
      </label>
      {children}
      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}
    </div>
  )
}

interface ModalProps {
  title: string
  onClose: () => void
  children: ReactNode
}

/** Lightweight accessible modal — Escape and backdrop click both close it. */
function Modal({ title, onClose, children }: ModalProps) {
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        aria-label="Close dialog"
        onClick={onClose}
        className="absolute inset-0 bg-black/50"
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="relative w-full max-w-lg overflow-hidden rounded-2xl border border-border bg-card shadow-xl"
      >
        <div className="flex items-center justify-between border-b border-border px-6 py-4">
          <h2 className="text-lg font-semibold">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
          >
            <X aria-hidden="true" className="h-4 w-4" />
          </button>
        </div>
        <div className="max-h-[calc(100vh-12rem)] overflow-y-auto px-6 py-5">
          {children}
        </div>
      </div>
    </div>
  )
}

interface FoodFormModalProps {
  food: FoodItem | null
  onClose: () => void
  onSaved: (food: FoodItem) => void
}

function FoodFormModal({ food, onClose, onSaved }: FoodFormModalProps) {
  const [submitError, setSubmitError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FoodFormValues>({
    defaultValues: food ? toFormValues(food) : EMPTY_FORM,
  })

  const onSubmit = async (values: FoodFormValues) => {
    setSubmitError(null)
    const payload = toPayload(values)
    try {
      const saved = food
        ? await foodService.updateFood(food.id, payload)
        : await foodService.createFood(payload)
      onSaved(saved)
    } catch (error) {
      setSubmitError(getErrorMessage(error))
    }
  }

  return (
    <Modal title={food ? 'Edit food' : 'Add food'} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
        {submitError && (
          <div
            role="alert"
            className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-400"
          >
            {submitError}
          </div>
        )}

        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Name" htmlFor="food-name" error={errors.name?.message}>
            <input
              id="food-name"
              type="text"
              className={INPUT_CLASSES}
              {...register('name', {
                required: 'Name is required',
                minLength: { value: 2, message: 'Name must be at least 2 characters' },
                maxLength: { value: 150, message: 'Name must be at most 150 characters' },
              })}
            />
          </Field>

          <Field label="Category" htmlFor="food-category" error={errors.category?.message}>
            <select
              id="food-category"
              className={INPUT_CLASSES}
              {...register('category', { required: 'Category is required' })}
            >
              <option value="" disabled>
                Select category
              </option>
              {CATEGORY_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </Field>
        </div>

        <Field label="Description" htmlFor="food-description" error={errors.description?.message}>
          <textarea
            id="food-description"
            rows={3}
            className={`${INPUT_CLASSES} resize-none`}
            {...register('description', {
              required: 'Description is required',
              maxLength: { value: 500, message: 'Description must be at most 500 characters' },
            })}
          />
        </Field>

        <div className="grid grid-cols-2 gap-4 sm:grid-cols-5">
          <Field label="Calories" htmlFor="food-calories" error={errors.calories?.message}>
            <input
              id="food-calories"
              type="number"
              inputMode="decimal"
              step="any"
              min={0}
              className={INPUT_CLASSES}
              {...register('calories', {
                required: 'Calories are required',
                min: { value: 0, message: 'Must be 0 or more' },
              })}
            />
          </Field>

          <Field label="Protein" htmlFor="food-protein" error={errors.protein?.message}>
            <input
              id="food-protein"
              type="number"
              inputMode="decimal"
              step="any"
              min={0}
              className={INPUT_CLASSES}
              {...register('protein', {
                required: 'Protein is required',
                min: { value: 0, message: 'Must be 0 or more' },
              })}
            />
          </Field>

          <Field label="Carbs" htmlFor="food-carbs" error={errors.carbohydrates?.message}>
            <input
              id="food-carbs"
              type="number"
              inputMode="decimal"
              step="any"
              min={0}
              className={INPUT_CLASSES}
              {...register('carbohydrates', {
                required: 'Carbohydrates are required',
                min: { value: 0, message: 'Must be 0 or more' },
              })}
            />
          </Field>

          <Field label="Fat" htmlFor="food-fat" error={errors.fat?.message}>
            <input
              id="food-fat"
              type="number"
              inputMode="decimal"
              step="any"
              min={0}
              className={INPUT_CLASSES}
              {...register('fat', {
                required: 'Fat is required',
                min: { value: 0, message: 'Must be 0 or more' },
              })}
            />
          </Field>

          <Field label="Fiber" htmlFor="food-fiber" error={errors.fiber?.message}>
            <input
              id="food-fiber"
              type="number"
              inputMode="decimal"
              step="any"
              min={0}
              className={INPUT_CLASSES}
              {...register('fiber', {
                required: 'Fiber is required',
                min: { value: 0, message: 'Must be 0 or more' },
              })}
            />
          </Field>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field label="Serving size" htmlFor="food-serving" error={errors.servingSize?.message}>
            <input
              id="food-serving"
              type="text"
              placeholder="e.g. 100 g"
              className={INPUT_CLASSES}
              {...register('servingSize', {
                required: 'Serving size is required',
                maxLength: { value: 50, message: 'Serving size must be at most 50 characters' },
              })}
            />
          </Field>

          <Field label="Image URL" htmlFor="food-image" error={errors.imageUrl?.message}>
            <input
              id="food-image"
              type="url"
              placeholder="https://…"
              className={INPUT_CLASSES}
              {...register('imageUrl', {
                validate: (value) =>
                  value === '' ||
                  /^https?:\/\/.+/.test(value) ||
                  'Image URL must start with http:// or https://',
              })}
            />
          </Field>
        </div>

        <label className="flex cursor-pointer items-center gap-2.5 rounded-lg border border-border bg-background px-3.5 py-2.5 text-sm">
          <input
            type="checkbox"
            className="h-4 w-4 rounded border-border accent-primary"
            {...register('vegetarian')}
          />
          Vegetarian
        </label>

        <div className="flex items-center justify-end gap-3 border-t border-border pt-4">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="inline-flex items-center justify-center rounded-lg border border-border bg-background px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? (
              <>
                <Spinner size="sm" />
                Saving…
              </>
            ) : food ? (
              'Save changes'
            ) : (
              'Add food'
            )}
          </button>
        </div>
      </form>
    </Modal>
  )
}

/* ---------- page ---------- */

type Notice = { message: string; tone: 'success' | 'error' } | null

/**
 * Admin food management — create, edit and delete catalog entries.
 * Wrapped in AdminRoute; every mutating call uses the JWT the axios
 * interceptor attaches, which the backend checks against Role.ADMIN.
 */
export function AdminPage() {
  const [foods, setFoods] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  const [isFormOpen, setIsFormOpen] = useState(false)
  const [editingFood, setEditingFood] = useState<FoodItem | null>(null)
  const [deletingFood, setDeletingFood] = useState<FoodItem | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)

  useEffect(() => {
    let ignore = false

    const load = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const result = await foodService.getAllFoods()
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
  }, [reloadKey])

  const openAdd = () => {
    setEditingFood(null)
    setIsFormOpen(true)
  }

  const openEdit = (food: FoodItem) => {
    setEditingFood(food)
    setIsFormOpen(true)
  }

  const handleSaved = (saved: FoodItem) => {
    setFoods((prev) => {
      const exists = prev.some((food) => food.id === saved.id)
      return exists
        ? prev.map((food) => (food.id === saved.id ? saved : food))
        : [saved, ...prev]
    })
    setNotice({
      message: editingFood ? `"${saved.name}" updated.` : `"${saved.name}" added.`,
      tone: 'success',
    })
    setIsFormOpen(false)
    setEditingFood(null)
  }

  const handleDelete = async () => {
    if (!deletingFood) {
      return
    }
    setIsDeleting(true)
    try {
      await foodService.deleteFood(deletingFood.id)
      setFoods((prev) => prev.filter((food) => food.id !== deletingFood.id))
      setNotice({ message: `"${deletingFood.name}" deleted.`, tone: 'success' })
      setDeletingFood(null)
    } catch (err) {
      setNotice({ message: getErrorMessage(err), tone: 'error' })
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Admin</h1>
          <p className="mt-2 text-muted-foreground">
            Manage the food catalog — add, edit and remove entries.
          </p>
        </div>
        <button
          type="button"
          onClick={openAdd}
          className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
        >
          <Plus aria-hidden="true" className="h-4 w-4" />
          Add food
        </button>
      </header>

      {notice && (
        <div
          role="status"
          className={`mt-6 flex items-center justify-between gap-4 rounded-lg border px-4 py-3 text-sm ${
            notice.tone === 'success'
              ? 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-400'
              : 'border-red-200 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-400'
          }`}
        >
          <span>{notice.message}</span>
          <button
            type="button"
            onClick={() => setNotice(null)}
            aria-label="Dismiss"
            className="shrink-0 rounded-md p-0.5 transition-colors hover:bg-black/5 dark:hover:bg-white/10"
          >
            <X aria-hidden="true" className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Loading */}
      {isLoading && (
        <div className="mt-8">
          <p role="status" className="sr-only">
            Loading foods…
          </p>
          <div className="space-y-3">
            {Array.from({ length: 5 }, (_, index) => (
              <div key={index} className="h-12 animate-pulse rounded-lg bg-muted" />
            ))}
          </div>
        </div>
      )}

      {/* Error */}
      {!isLoading && error && (
        <div className="mt-8 flex flex-col items-center justify-center rounded-xl border border-border px-6 py-16 text-center">
          <h2 className="text-lg font-semibold">Couldn't load foods</h2>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">{error}</p>
          <button
            type="button"
            onClick={() => setReloadKey((key) => key + 1)}
            className="mt-5 inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Try again
          </button>
        </div>
      )}

      {/* Empty */}
      {!isLoading && !error && foods.length === 0 && (
        <div className="mt-8 flex flex-col items-center justify-center rounded-xl border border-dashed border-border px-6 py-16 text-center">
          <UtensilsCrossed aria-hidden="true" className="h-8 w-8 text-muted-foreground" />
          <h2 className="mt-4 text-lg font-semibold">No foods yet</h2>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">
            The catalog is empty. Add your first food to get started.
          </p>
          <button
            type="button"
            onClick={openAdd}
            className="mt-5 inline-flex items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            <Plus aria-hidden="true" className="h-4 w-4" />
            Add food
          </button>
        </div>
      )}

      {/* Table */}
      {!isLoading && !error && foods.length > 0 && (
        <div className="mt-8 overflow-hidden rounded-xl border border-border bg-card">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-border bg-muted/50 text-xs uppercase tracking-wider text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 font-medium">Food</th>
                  <th className="px-4 py-3 font-medium">Category</th>
                  <th className="px-4 py-3 font-medium">Calories</th>
                  <th className="px-4 py-3 font-medium">Protein</th>
                  <th className="px-4 py-3 font-medium">Carbs</th>
                  <th className="px-4 py-3 font-medium">Fat</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {foods.map((food) => (
                  <tr key={food.id} className="transition-colors hover:bg-muted/40">
                    <td className="px-4 py-3">
                      <div className="font-medium">{food.name}</div>
                      {food.vegetarian && (
                        <div className="text-xs text-emerald-600 dark:text-emerald-400">
                          Vegetarian
                        </div>
                      )}
                    </td>
                    <td className="px-4 py-3 text-muted-foreground">
                      {CATEGORY_LABELS[food.category]}
                    </td>
                    <td className="px-4 py-3">{Math.round(food.calories)} kcal</td>
                    <td className="px-4 py-3 text-muted-foreground">
                      {formatMacro(food.protein)}g
                    </td>
                    <td className="px-4 py-3 text-muted-foreground">
                      {formatMacro(food.carbohydrates)}g
                    </td>
                    <td className="px-4 py-3 text-muted-foreground">
                      {formatMacro(food.fat)}g
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <button
                          type="button"
                          onClick={() => openEdit(food)}
                          aria-label={`Edit ${food.name}`}
                          className="rounded-md p-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                        >
                          <Pencil aria-hidden="true" className="h-4 w-4" />
                        </button>
                        <button
                          type="button"
                          onClick={() => setDeletingFood(food)}
                          aria-label={`Delete ${food.name}`}
                          className="rounded-md p-2 text-muted-foreground transition-colors hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-950/50"
                        >
                          <Trash2 aria-hidden="true" className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add / Edit modal */}
      {isFormOpen && (
        <FoodFormModal
          key={editingFood?.id ?? 'new'}
          food={editingFood}
          onClose={() => {
            setIsFormOpen(false)
            setEditingFood(null)
          }}
          onSaved={handleSaved}
        />
      )}

      {/* Delete confirmation */}
      {deletingFood && (
        <Modal title="Delete food" onClose={() => setDeletingFood(null)}>
          <p className="text-sm text-muted-foreground">
            Are you sure you want to delete{' '}
            <span className="font-medium text-foreground">"{deletingFood.name}"</span>?
            This action cannot be undone.
          </p>
          <div className="mt-6 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setDeletingFood(null)}
              disabled={isDeleting}
              className="inline-flex items-center justify-center rounded-lg border border-border bg-background px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleDelete}
              disabled={isDeleting}
              className="inline-flex items-center justify-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isDeleting ? (
                <>
                  <Spinner size="sm" />
                  Deleting…
                </>
              ) : (
                'Delete'
              )}
            </button>
          </div>
        </Modal>
      )}
    </div>
  )
}