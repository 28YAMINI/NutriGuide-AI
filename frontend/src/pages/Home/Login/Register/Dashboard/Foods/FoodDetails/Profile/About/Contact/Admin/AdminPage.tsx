import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'

import { Leaf, PackageOpen, Pencil, Plus, SearchX, Trash2 } from 'lucide-react'
import { CATEGORY_LABELS } from '../../../../../../../../../../../constants/food'
import type { CreateFoodRequest, FoodCategory, FoodItem, UpdateFoodRequest } from '../../../../../../../../../../../types/food'
import { foodService } from '../../../../../../../../../../../services/foodService'
import { getErrorMessage } from '../../../../../../../../../../../utils/error'
import { Alert } from '../../../../../../../../../../../components/ui/Alert'
import { Button } from '../../../../../../../../../../../components/ui/Button'
import { PageState } from '../../../../../../../../../../../components/ui/PageState'
import { Card } from '../../../../../../../../../../../components/ui/Card'
import { formatCalories, formatMacro } from '../../../../../../../../../../../utils/format'
import { Modal } from '../../../../../../../../../../../components/ui/Modal'
import { Field, INPUT_CLASSES } from '../../../../../../../../../../../components/ui/Field'



const CATEGORIES = Object.keys(CATEGORY_LABELS) as FoodCategory[]

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

const nameRule = {
  required: 'Name is required',
  minLength: { value: 2, message: 'Name must be at least 2 characters' },
  maxLength: { value: 150, message: 'Name must be at most 150 characters' },
}

const descriptionRule = {
  required: 'Description is required',
  maxLength: {
    value: 500,
    message: 'Description must be at most 500 characters',
  },
}

const categoryRule = {
  required: 'Category is required',
}

const servingSizeRule = {
  required: 'Serving size is required',
  maxLength: {
    value: 50,
    message: 'Serving size must be at most 50 characters',
  },
}

/** Numeric string rule: required, a valid number, within [0, max]. */
function numericRule(label: string, max = 9999.99) {
  return {
    required: `${label} is required`,
    validate: (value: string) => {
      const num = Number(value)
      if (value.trim() === '' || Number.isNaN(num)) {
        return `${label} must be a number`
      }
      if (num < 0) return `${label} must be 0 or more`
      if (num > max) return `${label} must be at most ${max}`
      return true
    },
  }
}

const imageUrlRule = {
  validate: (value: string) =>
    value.trim() === '' ||
    /^(http|https):\/\/.*$/.test(value.trim()) ||
    'Image URL must start with http:// or https://',
}

/** Shape accepted by both createFood and updateFood (fields are identical). */
type FoodPayload = CreateFoodRequest & UpdateFoodRequest

/** Builds the API payload from raw form values (strings → numbers). */
function toPayload(values: FoodFormValues): FoodPayload {
  return {
    name: values.name.trim(),
    description: values.description.trim(),
    // The category select is validated as required, so '' cannot reach here.
    category: values.category as FoodCategory,
    calories: Number(values.calories),
    protein: Number(values.protein),
    carbohydrates: Number(values.carbohydrates),
    fat: Number(values.fat),
    fiber: Number(values.fiber),
    servingSize: values.servingSize.trim(),
    imageUrl: values.imageUrl.trim(),
    vegetarian: values.vegetarian,
  }
}

function AdminTableSkeleton() {
  return (
    <Card className="overflow-hidden">
      <div className="divide-y divide-border">
        {[0, 1, 2, 3, 4].map((i) => (
          <div key={i} className="flex items-center justify-between px-5 py-4">
            <div className="h-4 w-40 animate-pulse rounded bg-muted" />
            <div className="h-4 w-16 animate-pulse rounded bg-muted" />
          </div>
        ))}
      </div>
    </Card>
  )
}

type Notice = { tone: 'success' | 'error'; text: string } | null

/** Admin-only food management — full CRUD against the catalog API. */
export function AdminPage() {
  const [foods, setFoods] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  // Object state — no string state exists in this file, so
  // SetStateAction<string> can never be produced here.
  const [loadError, setLoadError] = useState<{ message: string } | null>(null)
  const [notice, setNotice] = useState<Notice>(null)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<FoodItem | null>(null)
  const [foodToDelete, setFoodToDelete] = useState<FoodItem | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FoodFormValues>({ defaultValues: EMPTY_FORM })

  const load = useCallback(async () => {
    setIsLoading(true)
    setLoadError(null)
    try {
      setFoods(await foodService.getAllFoods())
    } catch (err) {
      setLoadError({ message: getErrorMessage(err) })
      setFoods([])
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const openAdd = () => {
    setEditing(null)
    reset(EMPTY_FORM)
    setFormOpen(true)
  }

  const openEdit = (food: FoodItem) => {
    setEditing(food)
    reset({
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
    })
    setFormOpen(true)
  }

  const onSubmit = async (values: FoodFormValues) => {
    setNotice(null)
    try {
      const payload = toPayload(values)
      if (editing) {
        await foodService.updateFood(editing.id, payload)
        setNotice({ tone: 'success', text: `“${payload.name}” updated.` })
      } else {
        await foodService.createFood(payload)
        setNotice({
          tone: 'success',
          text: `“${payload.name}” added to the catalog.`,
        })
      }
      setFormOpen(false)
      void load()
    } catch (err) {
      setNotice({ tone: 'error', text: getErrorMessage(err) })
    }
  }

  const confirmDelete = async () => {
    if (!foodToDelete) return
    setIsDeleting(true)
    setNotice(null)
    try {
      await foodService.deleteFood(foodToDelete.id)
      setNotice({ tone: 'success', text: `“${foodToDelete.name}” deleted.` })
      setFoodToDelete(null)
      void load()
    } catch (err) {
      setNotice({ tone: 'error', text: getErrorMessage(err) })
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <main className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Admin</h1>
          <p className="mt-1 text-muted-foreground">
            Manage the food catalog — add, edit and remove entries.
          </p>
        </div>
        <Button onClick={openAdd}>
          <Plus aria-hidden="true" className="h-4 w-4" />
          Add food
        </Button>
      </header>

      {notice ? (
        <div className="mt-6">
          <Alert tone={notice.tone} onDismiss={() => setNotice(null)}>
            {notice.text}
          </Alert>
        </div>
      ) : null}

      <div className="mt-6">
        {isLoading ? (
          <AdminTableSkeleton />
        ) : loadError ? (
          <PageState
            icon={SearchX}
            title="Couldn't load foods"
            message={loadError.message}
            action={
              <Button variant="outline" onClick={() => void load()}>
                Try again
              </Button>
            }
          />
        ) : foods.length === 0 ? (
          <PageState
            icon={PackageOpen}
            title="No foods yet"
            message="Add your first food to start building the catalog."
            action={
              <Button onClick={openAdd}>
                <Plus aria-hidden="true" className="h-4 w-4" />
                Add food
              </Button>
            }
          />
        ) : (
          <Card className="overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-border bg-muted/50 text-xs uppercase tracking-wide text-muted-foreground">
                  <tr>
                    <th scope="col" className="px-5 py-3 font-medium">Food</th>
                    <th scope="col" className="px-5 py-3 font-medium">Category</th>
                    <th scope="col" className="px-5 py-3 font-medium">Calories</th>
                    <th scope="col" className="px-5 py-3 font-medium">Protein</th>
                    <th scope="col" className="px-5 py-3 font-medium">Vegetarian</th>
                    <th scope="col" className="px-5 py-3 text-right font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {foods.map((food) => (
                    <tr key={food.id} className="transition-colors hover:bg-muted/40">
                      <td className="px-5 py-3 font-medium text-foreground">{food.name}</td>
                      <td className="px-5 py-3 text-muted-foreground">
                        {CATEGORY_LABELS[food.category]}
                      </td>
                      <td className="px-5 py-3 text-muted-foreground">
                        {formatCalories(food.calories)}
                      </td>
                      <td className="px-5 py-3 text-muted-foreground">
                        {formatMacro(food.protein)} g
                      </td>
                      <td className="px-5 py-3">
                        {food.vegetarian ? (
                          <span className="inline-flex items-center gap-1 rounded-full bg-emerald-600/10 px-2 py-0.5 text-xs font-medium text-emerald-600 dark:text-emerald-400">
                            <Leaf aria-hidden="true" className="h-3 w-3" />
                            Yes
                          </span>
                        ) : (
                          <span className="text-muted-foreground">No</span>
                        )}
                      </td>
                      <td className="px-5 py-3">
                        <div className="flex justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => openEdit(food)}
                            aria-label={`Edit ${food.name}`}
                          >
                            <Pencil aria-hidden="true" className="h-4 w-4" />
                            Edit
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => setFoodToDelete(food)}
                            aria-label={`Delete ${food.name}`}
                          >
                            <Trash2
                              aria-hidden="true"
                              className="h-4 w-4 text-red-600 dark:text-red-400"
                            />
                            <span className="text-red-600 dark:text-red-400">
                              Delete
                            </span>
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        )}
      </div>

      {/* Add / edit modal */}
      <Modal
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editing ? 'Edit food' : 'Add food'}
        description={
          editing ? `Update “${editing.name}”.` : 'Create a new entry in the food catalog.'
        }
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => setFormOpen(false)}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" form="food-form" isLoading={isSubmitting}>
              {editing ? 'Save changes' : 'Add food'}
            </Button>
          </>
        }
      >
        <form id="food-form" onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-4">
          <Field label="Name" htmlFor="food-name" error={errors.name?.message}>
            <input
              id="food-name"
              className={INPUT_CLASSES}
              placeholder="e.g. Greek yogurt"
              aria-invalid={errors.name ? true : undefined}
              aria-describedby={errors.name ? 'food-name-error' : undefined}
              {...register('name', nameRule)}
            />
          </Field>

          <Field label="Description" htmlFor="food-description" error={errors.description?.message}>
            <textarea
              id="food-description"
              rows={3}
              className={INPUT_CLASSES}
              placeholder="A short description of the food"
              aria-invalid={errors.description ? true : undefined}
              aria-describedby={errors.description ? 'food-description-error' : undefined}
              {...register('description', descriptionRule)}
            />
          </Field>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Category" htmlFor="food-category" error={errors.category?.message}>
              <select
                id="food-category"
                className={INPUT_CLASSES}
                aria-invalid={errors.category ? true : undefined}
                aria-describedby={errors.category ? 'food-category-error' : undefined}
                {...register('category', categoryRule)}
              >
                <option value="">Select a category</option>
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {CATEGORY_LABELS[cat]}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="Serving size" htmlFor="food-serving" error={errors.servingSize?.message}>
              <input
                id="food-serving"
                className={INPUT_CLASSES}
                placeholder="e.g. 100 g"
                aria-invalid={errors.servingSize ? true : undefined}
                aria-describedby={errors.servingSize ? 'food-serving-error' : undefined}
                {...register('servingSize', servingSizeRule)}
              />
            </Field>
          </div>

          <div className="grid grid-cols-2 gap-4 sm:grid-cols-5">
            <Field label="Calories" htmlFor="food-calories" error={errors.calories?.message}>
              <input
                id="food-calories"
                type="number"
                inputMode="decimal"
                min={0}
                step="any"
                className={INPUT_CLASSES}
                placeholder="kcal"
                aria-invalid={errors.calories ? true : undefined}
                aria-describedby={errors.calories ? 'food-calories-error' : undefined}
                {...register('calories', numericRule('Calories'))}
              />
            </Field>
            <Field label="Protein" htmlFor="food-protein" error={errors.protein?.message}>
              <input
                id="food-protein"
                type="number"
                inputMode="decimal"
                min={0}
                step="any"
                className={INPUT_CLASSES}
                placeholder="g"
                aria-invalid={errors.protein ? true : undefined}
                aria-describedby={errors.protein ? 'food-protein-error' : undefined}
                {...register('protein', numericRule('Protein'))}
              />
            </Field>
            <Field label="Carbs" htmlFor="food-carbs" error={errors.carbohydrates?.message}>
              <input
                id="food-carbs"
                type="number"
                inputMode="decimal"
                min={0}
                step="any"
                className={INPUT_CLASSES}
                placeholder="g"
                aria-invalid={errors.carbohydrates ? true : undefined}
                aria-describedby={errors.carbohydrates ? 'food-carbs-error' : undefined}
                {...register('carbohydrates', numericRule('Carbs'))}
              />
            </Field>
            <Field label="Fat" htmlFor="food-fat" error={errors.fat?.message}>
              <input
                id="food-fat"
                type="number"
                inputMode="decimal"
                min={0}
                step="any"
                className={INPUT_CLASSES}
                placeholder="g"
                aria-invalid={errors.fat ? true : undefined}
                aria-describedby={errors.fat ? 'food-fat-error' : undefined}
                {...register('fat', numericRule('Fat'))}
              />
            </Field>
            <Field label="Fiber" htmlFor="food-fiber" error={errors.fiber?.message}>
              <input
                id="food-fiber"
                type="number"
                inputMode="decimal"
                min={0}
                step="any"
                className={INPUT_CLASSES}
                placeholder="g"
                aria-invalid={errors.fiber ? true : undefined}
                aria-describedby={errors.fiber ? 'food-fiber-error' : undefined}
                {...register('fiber', numericRule('Fiber'))}
              />
            </Field>
          </div>

          <Field label="Image URL (optional)" htmlFor="food-image" error={errors.imageUrl?.message}>
            <input
              id="food-image"
              type="url"
              className={INPUT_CLASSES}
              placeholder="https://…"
              aria-invalid={errors.imageUrl ? true : undefined}
              aria-describedby={errors.imageUrl ? 'food-image-error' : undefined}
              {...register('imageUrl', imageUrlRule)}
            />
          </Field>

          <label className="flex items-center gap-2 text-sm font-medium text-foreground">
            <input
              type="checkbox"
              className="h-4 w-4 rounded border-border accent-primary"
              {...register('vegetarian')}
            />
            Vegetarian
          </label>
        </form>
      </Modal>

      {/* Delete confirmation modal */}
      <Modal
        open={foodToDelete !== null}
        onClose={() => setFoodToDelete(null)}
        title="Delete food"
        description={
          foodToDelete
            ? `“${foodToDelete.name}” will be permanently removed.`
            : undefined
        }
        footer={
          <>
            <Button
              variant="outline"
              onClick={() => setFoodToDelete(null)}
              disabled={isDeleting}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={() => void confirmDelete()}
              isLoading={isDeleting}
            >
              Delete food
            </Button>
          </>
        }
      >
        <p className="text-sm text-muted-foreground">
          Are you sure? This action cannot be undone.
        </p>
      </Modal>
    </main>
  )
}