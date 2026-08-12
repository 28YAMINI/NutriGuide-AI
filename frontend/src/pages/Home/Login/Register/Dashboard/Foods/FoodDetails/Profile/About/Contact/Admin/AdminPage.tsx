import { useEffect, useState } from 'react'
import axios from 'axios'

import { Leaf, Pencil, Plus, Trash2, Utensils } from 'lucide-react'
import type { CreateFoodRequest, FoodCategory, FoodItem } from '../../../../../../../../../../../types/food'
import { foodService } from '../../../../../../../../../../../services/foodService'
import { useForm } from 'react-hook-form'
import { Button } from '../../../../../../../../../../../components/ui/Button'
import { PageState } from '../../../../../../../../../../../components/ui/PageState'
import { Alert } from '../../../../../../../../../../../components/ui/Alert'
import { FoodImage } from '../../../../../../../../../../../components/common/FoodImage'
import { CATEGORY_LABELS, FOOD_CATEGORIES } from '../../../../../../../../../../../constants/food'
import { Modal } from '../../../../../../../../../../../components/ui/Modal'
import { Field, INPUT_CLASSES } from '../../../../../../../../../../../components/ui/Field'

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
  imageUrl?: string
  vegetarian: boolean
}

type Notice = { tone: 'success' | 'error'; text: string } | null

const DEFAULT_FORM_VALUES: FoodFormValues = {
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

const NUMBER_RULE = (label: string) => ({
  required: `${label} is required`,
  min: { value: 0, message: `${label} must be 0 or greater` },
})

/**
 * Admin food management.
 *
 * Table CRUD for the food catalog: add / edit through a shared modal
 * form, delete through a confirmation dialog. All API calls go
 * through foodService — no contract changes.
 */
export function AdminPage() {
  const [foods, setFoods] = useState<FoodItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<Notice>(null)
  const [reloadKey, setReloadKey] = useState(0)

  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingFood, setEditingFood] = useState<FoodItem | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<FoodItem | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<FoodFormValues>({ defaultValues: DEFAULT_FORM_VALUES })

  // Load the catalog.
  useEffect(() => {
    let cancelled = false

    const load = async () => {
      setIsLoading(true)
      setError(null)

      try {
        const result = await foodService.getAllFoods()
        if (!cancelled) setFoods(result)
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
  }, [reloadKey])

  const openAdd = () => {
    setEditingFood(null)
    reset(DEFAULT_FORM_VALUES)
    setIsModalOpen(true)
  }

  const openEdit = (food: FoodItem) => {
    setEditingFood(food)
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
    setIsModalOpen(true)
  }

  const closeModal = () => {
    setIsModalOpen(false)
    setEditingFood(null)
  }

  /** Converts form values to the shared request shape (no casts needed). */
  const toPayload = (values: FoodFormValues): CreateFoodRequest => ({
    name: values.name.trim(),
    description: values.description.trim(),
    category: values.category as FoodCategory,
    calories: Number(values.calories),
    protein: Number(values.protein),
    carbohydrates: Number(values.carbohydrates),
    fat: Number(values.fat),
    fiber: Number(values.fiber),
    servingSize: values.servingSize.trim(),
    imageUrl: values.imageUrl?.trim() || undefined,
    vegetarian: values.vegetarian,
  })

  const onSubmit = async (values: FoodFormValues) => {
    setNotice(null)

    try {
      const payload = toPayload(values)

      if (editingFood) {
        await foodService.updateFood(editingFood.foodId, payload)
        setNotice({ tone: 'success', text: 'Food updated successfully.' })
      } else {
        await foodService.createFood(payload)
        setNotice({ tone: 'success', text: 'Food added successfully.' })
      }

      setIsModalOpen(false)
      setEditingFood(null)
      setReloadKey((key) => key + 1)
    } catch (err) {
      setNotice({ tone: 'error', text: getErrorMessage(err) })
    }
  }

  const confirmDelete = async () => {
    if (!deleteTarget) return

    setIsDeleting(true)
    setNotice(null)

    try {
      await foodService.deleteFood(deleteTarget.foodId)
      setNotice({ tone: 'success', text: 'Food deleted.' })
      setDeleteTarget(null)
      setReloadKey((key) => key + 1)
    } catch (err) {
      setNotice({ tone: 'error', text: getErrorMessage(err) })
      setDeleteTarget(null)
    } finally {
      setIsDeleting(false)
    }
  }

  /* ---------- states ---------- */

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="h-8 w-40 rounded-lg bg-muted" />
        <div className="mt-2 h-4 w-64 max-w-full rounded bg-muted" />
        <div className="mt-8 space-y-3" aria-hidden="true">
          {[0, 1, 2, 3, 4, 5].map((index) => (
            <div
              key={index}
              className="h-16 animate-pulse rounded-xl border border-border bg-card"
            />
          ))}
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
          <Button onClick={() => setReloadKey((key) => key + 1)}>
            Try again
          </Button>
        }
      />
    )
  }

  if (foods.length === 0) {
    return (
      <PageState
        icon={Plus}
        title="No foods yet"
        message="Add your first food to start building the catalog."
        action={<Button onClick={openAdd}>Add food</Button>}
      />
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      {/* Header */}
      <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Admin</h1>
          <p className="mt-1 text-muted-foreground">
            Manage the food catalog.
          </p>
        </div>
        <Button onClick={openAdd}>
          <Plus aria-hidden="true" className="h-4 w-4" />
          Add food
        </Button>
      </header>

      {notice ? (
        <Alert
          tone={notice.tone}
          className="mt-6"
          onDismiss={() => setNotice(null)}
        >
          {notice.text}
        </Alert>
      ) : null}

      {/* Table */}
      <div className="mt-8 overflow-x-auto rounded-xl border border-border bg-card shadow-sm">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead>
            <tr className="border-b border-border bg-muted/50 text-xs uppercase tracking-wide text-muted-foreground">
              <th scope="col" className="px-4 py-3 font-medium">
                Food
              </th>
              <th scope="col" className="px-4 py-3 font-medium">
                Category
              </th>
              <th scope="col" className="px-4 py-3 font-medium">
                Calories
              </th>
              <th scope="col" className="px-4 py-3 font-medium">
                Protein
              </th>
              <th scope="col" className="px-4 py-3 font-medium">
                Vegetarian
              </th>
              <th scope="col" className="px-4 py-3 text-right font-medium">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {foods.map((food) => (
              <tr key={food.foodId} className="transition-colors hover:bg-muted/40">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    <FoodImage
                      src={food.imageUrl}
                      alt={food.name}
                      className="h-10 w-12 shrink-0 rounded-md"
                    />
                    <div className="min-w-0">
                      <p className="truncate font-medium text-foreground">
                        {food.name}
                      </p>
                      <p className="truncate text-xs text-muted-foreground">
                        {food.servingSize}
                      </p>
                    </div>
                  </div>
                </td>
                <td className="px-4 py-3">
                  <span className="inline-flex rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary">
                    {CATEGORY_LABELS[food.category]}
                  </span>
                </td>
                <td className="px-4 py-3 text-foreground">
                  {food.calories} kcal
                </td>
                <td className="px-4 py-3 text-foreground">{food.protein} g</td>
                <td className="px-4 py-3">
                  {food.vegetarian ? (
                    <span className="inline-flex items-center gap-1 text-xs font-medium text-primary">
                      <Leaf aria-hidden="true" className="h-3.5 w-3.5" />
                      Yes
                    </span>
                  ) : (
                    <span className="text-xs text-muted-foreground">No</span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => openEdit(food)}
                    >
                      <Pencil aria-hidden="true" className="h-3.5 w-3.5" />
                      Edit
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="border-destructive/30 text-destructive hover:bg-destructive/10"
                      onClick={() => setDeleteTarget(food)}
                    >
                      <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Add / Edit modal */}
      <Modal
        open={isModalOpen}
        onClose={closeModal}
        title={editingFood ? 'Edit food' : 'Add food'}
        description={
          editingFood
            ? `Editing "${editingFood.name}"`
            : 'Add a new food to the catalog.'
        }
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={closeModal}>
              Cancel
            </Button>
            <Button type="submit" form="food-form" isLoading={isSubmitting}>
              {editingFood ? 'Save changes' : 'Add food'}
            </Button>
          </>
        }
      >
        <form
          id="food-form"
          onSubmit={handleSubmit(onSubmit)}
          noValidate
          className="space-y-4"
        >
          <Field
            label="Name"
            htmlFor="name"
            error={errors.name?.message}
            required
          >
            <input
              id="name"
              type="text"
              placeholder="e.g. Grilled chicken breast"
              aria-invalid={errors.name ? true : undefined}
              aria-describedby={errors.name ? 'name-error' : undefined}
              className={INPUT_CLASSES}
              {...register('name', {
                required: 'Name is required',
                minLength: {
                  value: 2,
                  message: 'Name must be at least 2 characters',
                },
                maxLength: {
                  value: 100,
                  message: 'Name must be at most 100 characters',
                },
              })}
            />
          </Field>

          <Field
            label="Description"
            htmlFor="description"
            error={errors.description?.message}
            required
          >
            <textarea
              id="description"
              rows={3}
              placeholder="A short, helpful description…"
              aria-invalid={errors.description ? true : undefined}
              aria-describedby={
                errors.description ? 'description-error' : undefined
              }
              className={`${INPUT_CLASSES} h-auto resize-y py-2`}
              {...register('description', {
                required: 'Description is required',
                minLength: {
                  value: 10,
                  message: 'Description must be at least 10 characters',
                },
              })}
            />
          </Field>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Category"
              htmlFor="category"
              error={errors.category?.message}
              required
            >
              <select
                id="category"
                className={`${INPUT_CLASSES} cursor-pointer`}
                aria-invalid={errors.category ? true : undefined}
                aria-describedby={errors.category ? 'category-error' : undefined}
                {...register('category', { required: 'Category is required' })}
              >
                <option value="" disabled>
                  Select category
                </option>
                {FOOD_CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {CATEGORY_LABELS[cat]}
                  </option>
                ))}
              </select>
            </Field>

            <Field
              label="Serving size"
              htmlFor="servingSize"
              error={errors.servingSize?.message}
              required
            >
              <input
                id="servingSize"
                type="text"
                placeholder="e.g. 100 g"
                aria-invalid={errors.servingSize ? true : undefined}
                aria-describedby={
                  errors.servingSize ? 'servingSize-error' : undefined
                }
                className={INPUT_CLASSES}
                {...register('servingSize', {
                  required: 'Serving size is required',
                })}
              />
            </Field>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Calories"
              htmlFor="calories"
              error={errors.calories?.message}
              required
            >
              <input
                id="calories"
                type="number"
                inputMode="decimal"
                min={0}
                step="0.1"
                placeholder="e.g. 165"
                aria-invalid={errors.calories ? true : undefined}
                aria-describedby={errors.calories ? 'calories-error' : undefined}
                className={INPUT_CLASSES}
                {...register('calories', NUMBER_RULE('Calories'))}
              />
            </Field>

            <Field
              label="Protein (g)"
              htmlFor="protein"
              error={errors.protein?.message}
              required
            >
              <input
                id="protein"
                type="number"
                inputMode="decimal"
                min={0}
                step="0.1"
                aria-invalid={errors.protein ? true : undefined}
                aria-describedby={errors.protein ? 'protein-error' : undefined}
                className={INPUT_CLASSES}
                {...register('protein', NUMBER_RULE('Protein'))}
              />
            </Field>

            <Field
              label="Carbohydrates (g)"
              htmlFor="carbohydrates"
              error={errors.carbohydrates?.message}
              required
            >
              <input
                id="carbohydrates"
                type="number"
                inputMode="decimal"
                min={0}
                step="0.1"
                aria-invalid={errors.carbohydrates ? true : undefined}
                aria-describedby={
                  errors.carbohydrates ? 'carbohydrates-error' : undefined
                }
                className={INPUT_CLASSES}
                {...register('carbohydrates', NUMBER_RULE('Carbohydrates'))}
              />
            </Field>

            <Field
              label="Fat (g)"
              htmlFor="fat"
              error={errors.fat?.message}
              required
            >
              <input
                id="fat"
                type="number"
                inputMode="decimal"
                min={0}
                step="0.1"
                aria-invalid={errors.fat ? true : undefined}
                aria-describedby={errors.fat ? 'fat-error' : undefined}
                className={INPUT_CLASSES}
                {...register('fat', NUMBER_RULE('Fat'))}
              />
            </Field>

            <Field
              label="Fiber (g)"
              htmlFor="fiber"
              error={errors.fiber?.message}
              required
            >
              <input
                id="fiber"
                type="number"
                inputMode="decimal"
                min={0}
                step="0.1"
                aria-invalid={errors.fiber ? true : undefined}
                aria-describedby={errors.fiber ? 'fiber-error' : undefined}
                className={INPUT_CLASSES}
                {...register('fiber', NUMBER_RULE('Fiber'))}
              />
            </Field>
          </div>

          <Field
            label="Image URL"
            htmlFor="imageUrl"
            error={errors.imageUrl?.message}
            hint="Optional. Falls back to a placeholder if missing."
          >
            <input
              id="imageUrl"
              type="url"
              placeholder="https://…"
              aria-invalid={errors.imageUrl ? true : undefined}
              aria-describedby={
                errors.imageUrl ? 'imageUrl-error' : 'imageUrl-hint'
              }
              className={INPUT_CLASSES}
              {...register('imageUrl')}
            />
          </Field>

          <div className="flex items-center gap-2.5">
            <input
              id="vegetarian"
              type="checkbox"
              className="h-4 w-4 cursor-pointer rounded border-border accent-primary"
              {...register('vegetarian')}
            />
            <label
              htmlFor="vegetarian"
              className="text-sm font-medium text-foreground"
            >
              This food is vegetarian
            </label>
          </div>
        </form>
      </Modal>

      {/* Delete confirmation */}
      <Modal
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        title="Delete food"
        size="sm"
      >
        <p className="text-sm text-muted-foreground">
          Are you sure you want to delete{' '}
          <strong className="font-medium text-foreground">
            {deleteTarget?.name}
          </strong>
          ? This action cannot be undone.
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <Button variant="outline" onClick={() => setDeleteTarget(null)}>
            Cancel
          </Button>
          <Button
            variant="destructive"
            onClick={confirmDelete}
            isLoading={isDeleting}
          >
            Delete
          </Button>
        </div>
      </Modal>
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