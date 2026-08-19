import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import axios from 'axios'

import {
  Activity,
  Flame,
  Scale,
  Target,
  User as UserIcon,
} from 'lucide-react'

import type {
  ActivityLevel,
  Gender,
  Goal,
  UpdateUserRequest,
  UserResponse,
} from '../../types/user.ts'
import { useAuth } from '../../hooks/useAuth.ts'
import { userService } from '../../services/userService.ts'
import { routePaths } from '../../routes/routePaths.ts'
import { Field, INPUT_CLASSES } from '../../components/ui/Field.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Alert } from '../../components/ui/Alert.tsx'
import { Card } from '../../components/ui/Card.tsx'
import { PageState } from '../../components/ui/PageState.tsx'

interface ProfileFormValues {
  firstName: string
  lastName: string
  age: string
  gender: Gender | ''
  height: string
  weight: string
  activityLevel: ActivityLevel | ''
  goal: Goal | ''
}

const GENDER_OPTIONS: ReadonlyArray<{ value: Gender; label: string }> = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
]

const ACTIVITY_OPTIONS: ReadonlyArray<{
  value: ActivityLevel
  label: string
}> = [
  { value: 'SEDENTARY', label: 'Sedentary (little or no exercise)' },
  { value: 'LIGHT', label: 'Light (1–2 days/week)' },
  { value: 'MODERATE', label: 'Moderate (3–5 days/week)' },
  { value: 'ACTIVE', label: 'Active (6–7 days/week)' },
  { value: 'VERY_ACTIVE', label: 'Very active (intense daily exercise)' },
]

const GOAL_OPTIONS: ReadonlyArray<{ value: Goal; label: string }> = [
  { value: 'LOSE_WEIGHT', label: 'Lose weight' },
  { value: 'GAIN_WEIGHT', label: 'Gain weight' },
  { value: 'MAINTAIN_WEIGHT', label: 'Maintain weight' },
]



const GOAL_LABELS: Record<Goal, string> = {
  LOSE_WEIGHT: 'Lose weight',
  GAIN_WEIGHT: 'Gain weight',
  MAINTAIN_WEIGHT: 'Maintain weight',
}

/* ---------- health math (same formulas as the Dashboard) ---------- */

/** BMI from height (cm) and weight (kg). */
function bmiOf(user: UserResponse): number {
  const heightM = user.height / 100
  return user.weight / (heightM * heightM)
}

/** WHO classification for adult BMI values. */
function bmiCategory(bmi: number): string {
  if (bmi < 18.5) return 'Underweight'
  if (bmi < 25) return 'Normal'
  if (bmi < 30) return 'Overweight'
  return 'Obese'
}

/** BMR via the Mifflin-St Jeor equation. OTHER defaults to the male formula. */
function bmrOf(user: UserResponse): number {
  const base = 10 * user.weight + 6.25 * user.height - 5 * user.age
  return user.gender === 'FEMALE' ? base - 161 : base + 5
}

const ACTIVITY_MULTIPLIERS: Record<ActivityLevel, number> = {
  SEDENTARY: 1.2,
  LIGHT: 1.375,
  MODERATE: 1.55,
  ACTIVE: 1.725,
  VERY_ACTIVE: 1.9,
}

const GOAL_ADJUSTMENT: Record<Goal, number> = {
  LOSE_WEIGHT: -500,
  GAIN_WEIGHT: 500,
  MAINTAIN_WEIGHT: 0,
}

/** Daily calorie target = BMR × activity multiplier + goal adjustment. */
function dailyCaloriesOf(user: UserResponse): number {
  const bmr = bmrOf(user)
  const multiplier = ACTIVITY_MULTIPLIERS[user.activityLevel]
  const adjustment = GOAL_ADJUSTMENT[user.goal]
  return Math.round(bmr * multiplier + adjustment)
}

/** Formats calories with thousands separators, e.g. 2,143. */
function formatCalories(calories: number): string {
  return Math.round(calories).toLocaleString()
}

function ProfileSkeleton() {
  return (
    <div
      aria-hidden="true"
      className="mx-auto max-w-6xl animate-pulse px-4 py-10 sm:px-6 lg:px-8"
    >
      <div className="h-8 w-48 rounded-lg bg-muted" />
      <div className="mt-2 h-4 w-72 max-w-full rounded bg-muted" />
      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_300px]">
        <div className="h-96 rounded-2xl border border-border bg-card" />
        <div className="h-64 rounded-2xl border border-border bg-card" />
      </div>
    </div>
  )
}

/**
 * Profile page.
 *
 * Prefills the form from the authenticated user and saves via
 * userService.updateMe. The sidebar preview recalculates health
 * metrics live from the form values as the user types.
 */
export function ProfilePage() {
  const { user, isLoading, setUser } = useAuth()


  const [serverError, setServerError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormValues>({
    defaultValues: {
      firstName: '',
      lastName: '',
      age: '',
      gender: '',
      height: '',
      weight: '',
      activityLevel: '',
      goal: '',
    },
  })

  // Prefill the form whenever the authenticated user loads.
  useEffect(() => {
    if (!user) return
    reset({
      firstName: user.firstName,
      lastName: user.lastName,
      age: String(user.age),
      gender: user.gender,
      height: String(user.height),
      weight: String(user.weight),
      activityLevel: user.activityLevel,
      goal: user.goal,
    })
  }, [user, reset])

  const watched = watch()

  if (isLoading) {
    return <ProfileSkeleton />
  }

  if (!user) {
    return (
      <PageState
        icon={UserIcon}
        title="No profile data"
        message="Sign in to view and update your health profile."
        action={
          <Link to={routePaths.login} className={/* buttonClassName('primary','md') */ 'rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90'}>
            Sign in
          </Link>
        }
      />
    )
  }

  // Live values: fall back to the saved profile for any field the
  // user hasn't touched yet.
  const liveProfile: UserResponse = {
    ...user,
    firstName: watched.firstName.trim() || user.firstName,
    lastName: watched.lastName.trim() || user.lastName,
    age: Number(watched.age) || user.age,
    height: Number(watched.height) || user.height,
    weight: Number(watched.weight) || user.weight,
    gender: (watched.gender || user.gender) as Gender,
    activityLevel: (watched.activityLevel || user.activityLevel) as ActivityLevel,
    goal: (watched.goal || user.goal) as Goal,
  }

  const bmi = bmiOf(liveProfile)
  const bmr = bmrOf(liveProfile)
  const dailyCalories = dailyCaloriesOf(liveProfile)

  const previewRows = [
    { icon: Scale, label: 'BMI', value: `${bmi.toFixed(1)} · ${bmiCategory(bmi)}` },
    { icon: Flame, label: 'BMR', value: formatCalories(bmr) },
    { icon: Target, label: 'Daily target', value: formatCalories(dailyCalories) },
    { icon: Activity, label: 'Goal', value: GOAL_LABELS[liveProfile.goal] },
  ] as const

  const onSubmit = async (values: ProfileFormValues) => {
    setServerError(null)
    setSuccess(null)

    const payload: UpdateUserRequest = {
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      age: Number(values.age),
      gender: values.gender as Gender,
      height: Number(values.height),
      weight: Number(values.weight),
      activityLevel: values.activityLevel as ActivityLevel,
      goal: values.goal as Goal,
    }

    try {
      const updated = await userService.updateMe(payload)
      setUser(updated)
      setSuccess('Your profile has been updated.')
    } catch (error) {
      setServerError(getErrorMessage(error))
    }
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6 lg:px-8">
      {/* Header */}
      <header>
        <h1 className="text-3xl font-bold tracking-tight">Your profile</h1>
        <p className="mt-1 text-muted-foreground">
          Keep your health details up to date — your plan recalculates from
          them.
        </p>
      </header>

      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_300px] lg:items-start">
        {/* Form card */}
        <div className="rounded-2xl border border-border bg-card shadow-sm">
          {success ? (
            <div className="border-b border-border px-6 pt-5">
              <Alert tone="success" onDismiss={() => setSuccess(null)}>
                {success}
              </Alert>
            </div>
          ) : null}

          {serverError ? (
            <div className="border-b border-border px-6 pt-5">
              <Alert tone="error" onDismiss={() => setServerError(null)}>
                {serverError}
              </Alert>
            </div>
          ) : null}

          <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-6 p-6">
            {/* Account */}
            <section aria-labelledby="account-heading" className="space-y-4">
              <h2
                id="account-heading"
                className="text-xs font-semibold uppercase tracking-wider text-muted-foreground"
              >
                Account
              </h2>
              <div className="grid gap-4 sm:grid-cols-2">
                <Field
                  label="First name"
                  htmlFor="firstName"
                  error={errors.firstName?.message}
                  required
                >
                  <input
                    id="firstName"
                    type="text"
                    autoComplete="given-name"
                    aria-invalid={errors.firstName ? true : undefined}
                    aria-describedby={
                      errors.firstName ? 'firstName-error' : undefined
                    }
                    className={INPUT_CLASSES}
                    {...register('firstName', {
                      required: 'First name is required',
                      minLength: {
                        value: 2,
                        message: 'First name must be at least 2 characters',
                      },
                      maxLength: {
                        value: 50,
                        message: 'First name must be at most 50 characters',
                      },
                    })}
                  />
                </Field>

                <Field
                  label="Last name"
                  htmlFor="lastName"
                  error={errors.lastName?.message}
                  required
                >
                  <input
                    id="lastName"
                    type="text"
                    autoComplete="family-name"
                    aria-invalid={errors.lastName ? true : undefined}
                    aria-describedby={
                      errors.lastName ? 'lastName-error' : undefined
                    }
                    className={INPUT_CLASSES}
                    {...register('lastName', {
                      required: 'Last name is required',
                      minLength: {
                        value: 2,
                        message: 'Last name must be at least 2 characters',
                      },
                      maxLength: {
                        value: 50,
                        message: 'Last name must be at most 50 characters',
                      },
                    })}
                  />
                </Field>
              </div>
            </section>

            <div className="border-t border-border" />

            {/* Health profile */}
            <section aria-labelledby="health-heading" className="space-y-4">
              <h2
                id="health-heading"
                className="text-xs font-semibold uppercase tracking-wider text-muted-foreground"
              >
                Health profile
              </h2>

              <div className="grid gap-4 sm:grid-cols-3">
                <Field
                  label="Age"
                  htmlFor="age"
                  error={errors.age?.message}
                  required
                >
                  <input
                    id="age"
                    type="number"
                    inputMode="numeric"
                    min={13}
                    max={120}
                    aria-invalid={errors.age ? true : undefined}
                    aria-describedby={errors.age ? 'age-error' : undefined}
                    className={INPUT_CLASSES}
                    {...register('age', {
                      required: 'Age is required',
                      min: { value: 13, message: 'Age must be between 13 and 120' },
                      max: { value: 120, message: 'Age must be between 13 and 120' },
                    })}
                  />
                </Field>

                <Field
                  label="Height (cm)"
                  htmlFor="height"
                  error={errors.height?.message}
                  required
                >
                  <input
                    id="height"
                    type="number"
                    inputMode="decimal"
                    min={50}
                    max={250}
                    aria-invalid={errors.height ? true : undefined}
                    aria-describedby={errors.height ? 'height-error' : undefined}
                    className={INPUT_CLASSES}
                    {...register('height', {
                      required: 'Height is required',
                      min: { value: 50, message: 'Height must be between 50 and 250 cm' },
                      max: { value: 250, message: 'Height must be between 50 and 250 cm' },
                    })}
                  />
                </Field>

                <Field
                  label="Weight (kg)"
                  htmlFor="weight"
                  error={errors.weight?.message}
                  required
                >
                  <input
                    id="weight"
                    type="number"
                    inputMode="decimal"
                    min={20}
                    max={300}
                    aria-invalid={errors.weight ? true : undefined}
                    aria-describedby={errors.weight ? 'weight-error' : undefined}
                    className={INPUT_CLASSES}
                    {...register('weight', {
                      required: 'Weight is required',
                      min: { value: 20, message: 'Weight must be between 20 and 300 kg' },
                      max: { value: 300, message: 'Weight must be between 20 and 300 kg' },
                    })}
                  />
                </Field>
              </div>

              <Field
                label="Gender"
                htmlFor="gender"
                error={errors.gender?.message}
                required
              >
                <select
                  id="gender"
                  className={`${INPUT_CLASSES} cursor-pointer`}
                  aria-invalid={errors.gender ? true : undefined}
                  aria-describedby={errors.gender ? 'gender-error' : undefined}
                  {...register('gender', { required: 'Gender is required' })}
                >
                  {GENDER_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                label="Activity level"
                htmlFor="activityLevel"
                error={errors.activityLevel?.message}
                required
              >
                <select
                  id="activityLevel"
                  className={`${INPUT_CLASSES} cursor-pointer`}
                  aria-invalid={errors.activityLevel ? true : undefined}
                  aria-describedby={
                    errors.activityLevel ? 'activityLevel-error' : undefined
                  }
                  {...register('activityLevel', {
                    required: 'Activity level is required',
                  })}
                >
                  {ACTIVITY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                label="Goal"
                htmlFor="goal"
                error={errors.goal?.message}
                required
              >
                <select
                  id="goal"
                  className={`${INPUT_CLASSES} cursor-pointer`}
                  aria-invalid={errors.goal ? true : undefined}
                  aria-describedby={errors.goal ? 'goal-error' : undefined}
                  {...register('goal', { required: 'Goal is required' })}
                >
                  {GOAL_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </Field>
            </section>

            <div className="flex flex-col gap-3 border-t border-border pt-5 sm:flex-row sm:justify-end">
              <Button
                type="submit"
                size="lg"
                className="w-full sm:w-auto"
                isLoading={isSubmitting}
              >
                {isSubmitting ? 'Saving…' : 'Save changes'}
              </Button>
            </div>
          </form>
        </div>

        {/* Live preview */}
        <Card
          title="Live preview"
          description="Updates as you type"
          className="lg:sticky lg:top-24"
        >
          <div className="space-y-3">
            {previewRows.map((row) => (
              <div
                key={row.label}
                className="flex items-center justify-between gap-3 rounded-lg bg-muted/60 px-4 py-3"
              >
                <span className="flex items-center gap-2 text-sm text-muted-foreground">
                  <row.icon aria-hidden="true" className="h-4 w-4 text-primary" />
                  {row.label}
                </span>
                <span className="text-sm font-semibold text-foreground">
                  {row.value}
                </span>
              </div>
            ))}
          </div>
          <p className="mt-4 text-xs text-muted-foreground">
            Estimates use the Mifflin-St Jeor equation with your activity level
            and goal.
          </p>
        </Card>
      </div>
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