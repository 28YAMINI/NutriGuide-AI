import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { useForm } from 'react-hook-form'

import { Pencil, X } from 'lucide-react'
import type { ActivityLevel, Gender, Goal, UpdateUserRequest, UserResponse } from '../../../../../../../../types/user'
import { userService } from '../../../../../../../../services/userService'
import { useAuth } from '../../../../../../../../hooks/useAuth'
import { Spinner } from '../../../../../../../../components/common/Spinner'




/* ---------- shared form helpers ---------- */

const INPUT_CLASSES =
  'w-full rounded-lg border border-border bg-background px-3.5 py-2.5 text-sm text-foreground placeholder:text-muted-foreground transition-colors focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 disabled:cursor-not-allowed disabled:opacity-60'

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

function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  return 'Something went wrong. Please try again.'
}

/* ---------- option / label constants ---------- */

const GENDER_LABELS: Record<Gender, string> = {
  MALE: 'Male',
  FEMALE: 'Female',
  OTHER: 'Other',
}

const GENDER_OPTIONS = Object.entries(GENDER_LABELS).map(([value, label]) => ({
  value: value as Gender,
  label,
}))

const ACTIVITY_LABELS: Record<ActivityLevel, string> = {
  SEDENTARY: 'Sedentary',
  LIGHT: 'Lightly active',
  MODERATE: 'Moderately active',
  ACTIVE: 'Active',
  VERY_ACTIVE: 'Very active',
}

const ACTIVITY_OPTIONS: ReadonlyArray<{ value: ActivityLevel; label: string }> = [
  { value: 'SEDENTARY', label: 'Sedentary (little or no exercise)' },
  { value: 'LIGHT', label: 'Light (1–2 days/week)' },
  { value: 'MODERATE', label: 'Moderate (3–5 days/week)' },
  { value: 'ACTIVE', label: 'Active (6–7 days/week)' },
  { value: 'VERY_ACTIVE', label: 'Very active (intense daily exercise)' },
]

const GOAL_LABELS: Record<Goal, string> = {
  LOSE_WEIGHT: 'Lose weight',
  GAIN_WEIGHT: 'Gain weight',
  MAINTAIN_WEIGHT: 'Maintain weight',
}

const GOAL_OPTIONS = Object.entries(GOAL_LABELS).map(([value, label]) => ({
  value: value as Goal,
  label,
}))

/* ---------- small helpers ---------- */

function initials(user: UserResponse): string {
  return `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase()
}

/** WHO classification for adult BMI values. */
function bmiCategory(bmi: number): string {
  if (bmi < 18.5) return 'Underweight'
  if (bmi < 25) return 'Healthy range'
  if (bmi < 30) return 'Overweight'
  return 'Obese'
}

function bmiSummary(profile: UserResponse): string {
  const heightM = profile.height / 100
  if (heightM <= 0 || profile.weight <= 0) {
    return '—'
  }
  const bmi = profile.weight / (heightM * heightM)
  return `${bmi.toFixed(1)} · ${bmiCategory(bmi)}`
}

/* ---------- form ---------- */

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

interface ProfileFormProps {
  initialValues: ProfileFormValues
  onCancel: () => void
  onSaved: (updated: UserResponse) => void
}

function ProfileForm({ initialValues, onCancel, onSaved }: ProfileFormProps) {
  const [submitError, setSubmitError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormValues>({ defaultValues: initialValues })

  const onSubmit = async (values: ProfileFormValues) => {
    setSubmitError(null)

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
      onSaved(updated)
    } catch (error) {
      setSubmitError(getErrorMessage(error))
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="mt-8 rounded-xl border border-border bg-card p-6 sm:p-8"
    >
      <h2 className="text-lg font-semibold">Edit profile</h2>
      <p className="mt-1 text-sm text-muted-foreground">
        Changes apply to your account immediately.
      </p>

      {submitError && (
        <div
          role="alert"
          className="mt-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-400"
        >
          {submitError}
        </div>
      )}

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <Field label="First name" htmlFor="profile-firstName" error={errors.firstName?.message}>
          <input
            id="profile-firstName"
            type="text"
            className={INPUT_CLASSES}
            {...register('firstName', {
              required: 'First name is required',
              minLength: { value: 2, message: 'First name must be at least 2 characters' },
              maxLength: { value: 50, message: 'First name must be at most 50 characters' },
            })}
          />
        </Field>

        <Field label="Last name" htmlFor="profile-lastName" error={errors.lastName?.message}>
          <input
            id="profile-lastName"
            type="text"
            className={INPUT_CLASSES}
            {...register('lastName', {
              required: 'Last name is required',
              minLength: { value: 2, message: 'Last name must be at least 2 characters' },
              maxLength: { value: 50, message: 'Last name must be at most 50 characters' },
            })}
          />
        </Field>
      </div>

      <div className="mt-4 grid gap-4 sm:grid-cols-3">
        <Field label="Age" htmlFor="profile-age" error={errors.age?.message}>
          <input
            id="profile-age"
            type="number"
            inputMode="numeric"
            className={INPUT_CLASSES}
            {...register('age', {
              required: 'Age is required',
              min: { value: 13, message: 'Age must be between 13 and 120' },
              max: { value: 120, message: 'Age must be between 13 and 120' },
            })}
          />
        </Field>

        <Field label="Height (cm)" htmlFor="profile-height" error={errors.height?.message}>
          <input
            id="profile-height"
            type="number"
            inputMode="decimal"
            className={INPUT_CLASSES}
            {...register('height', {
              required: 'Height is required',
              min: { value: 50, message: 'Height must be between 50 and 250 cm' },
              max: { value: 250, message: 'Height must be between 50 and 250 cm' },
            })}
          />
        </Field>

        <Field label="Weight (kg)" htmlFor="profile-weight" error={errors.weight?.message}>
          <input
            id="profile-weight"
            type="number"
            inputMode="decimal"
            className={INPUT_CLASSES}
            {...register('weight', {
              required: 'Weight is required',
              min: { value: 20, message: 'Weight must be between 20 and 300 kg' },
              max: { value: 300, message: 'Weight must be between 20 and 300 kg' },
            })}
          />
        </Field>
      </div>

      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <Field label="Gender" htmlFor="profile-gender" error={errors.gender?.message}>
          <select
            id="profile-gender"
            className={INPUT_CLASSES}
            {...register('gender', { required: 'Gender is required' })}
          >
            <option value="" disabled>
              Select gender
            </option>
            {GENDER_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </Field>

        <Field
          label="Activity level"
          htmlFor="profile-activityLevel"
          error={errors.activityLevel?.message}
        >
          <select
            id="profile-activityLevel"
            className={INPUT_CLASSES}
            {...register('activityLevel', { required: 'Activity level is required' })}
          >
            <option value="" disabled>
              Select activity level
            </option>
            {ACTIVITY_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </Field>
      </div>

      <div className="mt-4">
        <Field label="Goal" htmlFor="profile-goal" error={errors.goal?.message}>
          <select
            id="profile-goal"
            className={INPUT_CLASSES}
            {...register('goal', { required: 'Goal is required' })}
          >
            <option value="" disabled>
              Select goal
            </option>
            {GOAL_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </Field>
      </div>

      <div className="mt-6 flex items-center justify-end gap-3 border-t border-border pt-5">
        <button
          type="button"
          onClick={onCancel}
          disabled={isSubmitting}
          className="inline-flex items-center gap-2 rounded-lg border border-border bg-background px-4 py-2 text-sm font-medium text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
        >
          <X aria-hidden="true" className="h-4 w-4" />
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
          ) : (
            'Save changes'
          )}
        </button>
      </div>
    </form>
  )
}

/* ---------- page ---------- */

function toFormValues(profile: UserResponse): ProfileFormValues {
  return {
    firstName: profile.firstName,
    lastName: profile.lastName,
    age: String(profile.age),
    gender: profile.gender,
    height: String(profile.height),
    weight: String(profile.weight),
    activityLevel: profile.activityLevel,
    goal: profile.goal,
  }
}

interface DetailTileProps {
  label: string
  value: string
}

function DetailTile({ label, value }: DetailTileProps) {
  return (
    <div className="rounded-xl border border-border bg-card p-4">
      <p className="text-xs font-medium uppercase tracking-wider text-muted-foreground">
        {label}
      </p>
      <p className="mt-1.5 text-sm font-semibold">{value}</p>
    </div>
  )
}

/**
 * Profile page — view + edit the authenticated user's own profile.
 * Protected route. Fresh data from GET /users/me; updates write back
 * into AuthContext via setUser so the whole app stays in sync.
 */
export function ProfilePage() {
  const { setUser } = useAuth()

  const [profile, setProfile] = useState<UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isEditing, setIsEditing] = useState(false)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    let ignore = false

    const load = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const result = await userService.getMe()
        if (!ignore) {
          setProfile(result)
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

  const handleSaved = (updated: UserResponse) => {
    setProfile(updated)
    setUser(updated)
    setIsEditing(false)
    setSuccessMessage('Profile updated successfully.')
  }

  if (isLoading) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-12 sm:px-6 lg:px-8">
        <p role="status" className="sr-only">
          Loading profile…
        </p>
        <div className="animate-pulse space-y-6">
          <div className="h-8 w-40 rounded bg-muted" />
          <div className="h-28 rounded-xl bg-muted" />
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 7 }, (_, index) => (
              <div key={index} className="h-20 rounded-xl bg-muted" />
            ))}
          </div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto max-w-4xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="flex flex-col items-center justify-center rounded-xl border border-border px-6 py-16 text-center">
          <h1 className="text-lg font-semibold">Couldn't load your profile</h1>
          <p className="mt-1 max-w-md text-sm text-muted-foreground">{error}</p>
          <button
            type="button"
            onClick={() => setReloadKey((key) => key + 1)}
            className="mt-5 inline-flex items-center justify-center rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Try again
          </button>
        </div>
      </div>
    )
  }

  if (!profile) {
    return null
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-12 sm:px-6 lg:px-8">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Profile</h1>
          <p className="mt-2 text-muted-foreground">
            Your account and health details.
          </p>
        </div>
        {!isEditing && (
          <button
            type="button"
            onClick={() => setIsEditing(true)}
            className="inline-flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            <Pencil aria-hidden="true" className="h-4 w-4" />
            Edit profile
          </button>
        )}
      </header>

      {successMessage && (
        <div
          role="status"
          className="mt-6 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700 dark:border-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-400"
        >
          {successMessage}
        </div>
      )}

      {isEditing ? (
        <ProfileForm
          key={profile.userId}
          initialValues={toFormValues(profile)}
          onCancel={() => setIsEditing(false)}
          onSaved={handleSaved}
        />
      ) : (
        <>
          {/* Identity card */}
          <div className="mt-8 flex flex-wrap items-center gap-4 rounded-xl border border-border bg-card p-6 sm:p-8">
            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-primary/10 text-base font-semibold text-primary">
              {initials(profile)}
            </div>
            <div className="min-w-0 flex-1">
              <h2 className="text-xl font-semibold">
                {profile.firstName} {profile.lastName}
              </h2>
              <p className="truncate text-sm text-muted-foreground">{profile.email}</p>
            </div>
            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-primary">
              {profile.role}
            </span>
          </div>

          {/* Health details */}
          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <DetailTile label="Age" value={String(profile.age)} />
            <DetailTile label="Gender" value={GENDER_LABELS[profile.gender]} />
            <DetailTile label="Height" value={`${profile.height} cm`} />
            <DetailTile label="Weight" value={`${profile.weight} kg`} />
            <DetailTile
              label="Activity level"
              value={ACTIVITY_LABELS[profile.activityLevel]}
            />
            <DetailTile label="Goal" value={GOAL_LABELS[profile.goal]} />
            <DetailTile label="BMI" value={bmiSummary(profile)} />
          </div>
        </>
      )}
    </div>
  )
}