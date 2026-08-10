import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'

import { Pencil, Save, User as UserIcon } from 'lucide-react'
import type { ActivityLevel, Gender, Goal, UpdateUserRequest, UserResponse } from '../../../../../../../../types/user'
import { useAuth } from '../../../../../../../../hooks/useAuth'
import { userService } from '../../../../../../../../services/userService'
import { PageState } from '../../../../../../../../components/ui/PageState'
import { routePaths } from '../../../../../../../../routes/routePaths'
import { Button } from '../../../../../../../../components/ui/Button'
import { Alert } from '../../../../../../../../components/ui/Alert'
import { Card } from '../../../../../../../../components/ui/Card'
import { Field, INPUT_CLASSES } from '../../../../../../../../components/ui/Field'
import { getErrorMessage } from '../../../../../../../../utils/error'

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

const EMPTY_FORM: ProfileFormValues = {
  firstName: '',
  lastName: '',
  age: '',
  gender: '',
  height: '',
  weight: '',
  activityLevel: '',
  goal: '',
}

const GENDER_OPTIONS: ReadonlyArray<{ value: Gender; label: string }> = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
]

const ACTIVITY_OPTIONS: ReadonlyArray<{ value: ActivityLevel; label: string }> = [
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

const GENDER_LABELS: Record<Gender, string> = {
  MALE: 'Male',
  FEMALE: 'Female',
  OTHER: 'Other',
}

const ACTIVITY_LABELS: Record<ActivityLevel, string> = {
  SEDENTARY: 'Sedentary',
  LIGHT: 'Lightly active',
  MODERATE: 'Moderately active',
  ACTIVE: 'Active',
  VERY_ACTIVE: 'Very active',
}

const GOAL_LABELS: Record<Goal, string> = {
  LOSE_WEIGHT: 'Lose weight',
  GAIN_WEIGHT: 'Gain weight',
  MAINTAIN_WEIGHT: 'Maintain weight',
}

function nameRule(label: string) {
  return {
    required: `${label} is required`,
    minLength: { value: 2, message: `${label} must be at least 2 characters` },
    maxLength: { value: 50, message: `${label} must be at most 50 characters` },
  }
}

/** Numeric string rule: required, a valid number within [min, max]. */
function numberRule(label: string, min: number, max: number) {
  return {
    required: `${label} is required`,
    validate: (value: string) => {
      const num = Number(value)
      if (value.trim() === '' || Number.isNaN(num)) {
        return `${label} must be a number`
      }
      if (num < min || num > max) {
        return `${label} must be between ${min} and ${max}`
      }
      return true
    },
  }
}

/** Maps a user entity into the form's string-based values. */
function formFromUser(user: UserResponse): ProfileFormValues {
  return {
    firstName: user.firstName,
    lastName: user.lastName,
    age: String(user.age),
    gender: user.gender,
    height: String(user.height),
    weight: String(user.weight),
    activityLevel: user.activityLevel,
    goal: user.goal,
  }
}

/** Builds the API payload from raw form values (strings → numbers). */
function toPayload(values: ProfileFormValues): UpdateUserRequest {
  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    age: Number(values.age),
    // The selects are validated as required, so '' cannot reach here.
    gender: values.gender as Gender,
    height: Number(values.height),
    weight: Number(values.weight),
    activityLevel: values.activityLevel as ActivityLevel,
    goal: values.goal as Goal,
  }
}

type Notice = { tone: 'success' | 'error'; text: string } | null

function ProfileSkeleton() {
  return (
    <div className="mx-auto max-w-4xl animate-pulse space-y-6 px-4 py-10 sm:px-6 lg:px-8">
      <div className="h-8 w-40 rounded-lg bg-muted" />
      <div className="h-4 w-64 rounded bg-muted" />
      <div className="grid gap-4 lg:grid-cols-2">
        <div className="h-48 rounded-xl border border-border bg-card" />
        <div className="h-48 rounded-xl border border-border bg-card" />
      </div>
    </div>
  )
}

/** Signed-in profile — view account + health data and edit it. */
export function ProfilePage() {
  const { user, setUser, isLoading } = useAuth()
  const [isEditing, setIsEditing] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormValues>({ defaultValues: EMPTY_FORM })

  useEffect(() => {
    if (user) reset(formFromUser(user))
  }, [user, reset])

  const onSubmit = async (values: ProfileFormValues) => {
    setNotice(null)
    try {
      const updated = await userService.updateMe(toPayload(values))
      setUser(updated)
      setNotice({ tone: 'success', text: 'Profile updated successfully.' })
      setIsEditing(false)
    } catch (err) {
      setNotice({ tone: 'error', text: getErrorMessage(err) })
    }
  }

  const cancelEdit = () => {
    setIsEditing(false)
    setNotice(null)
    if (user) reset(formFromUser(user))
  }

  if (isLoading) {
    return <ProfileSkeleton />
  }

  if (!user) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
        <PageState
          icon={UserIcon}
          title="No profile data"
          message="Sign in to view and update your profile."
          action={
            <Link to={routePaths.login} className="...">
              Sign in
            </Link>
          }
        />
      </main>
    )
  }

  const healthRows = [
    { label: 'Age', value: String(user.age) },
    { label: 'Gender', value: GENDER_LABELS[user.gender] },
    { label: 'Height', value: `${user.height} cm` },
    { label: 'Weight', value: `${user.weight} kg` },
    { label: 'Activity level', value: ACTIVITY_LABELS[user.activityLevel] },
    { label: 'Goal', value: GOAL_LABELS[user.goal] },
  ]

  return (
    <main className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
      <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Profile</h1>
          <p className="mt-1 text-muted-foreground">
            Your account and the health data driving your plan.
          </p>
        </div>
        {!isEditing ? (
          <Button variant="outline" onClick={() => setIsEditing(true)}>
            <Pencil aria-hidden="true" className="h-4 w-4" />
            Edit profile
          </Button>
        ) : null}
      </header>

      {notice ? (
        <div className="mt-6">
          <Alert tone={notice.tone} onDismiss={() => setNotice(null)}>
            {notice.text}
          </Alert>
        </div>
      ) : null}

      {isEditing ? (
        <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-6 space-y-6">
          <Card title="Account" description="Your personal details">
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="First name" htmlFor="profile-first-name" error={errors.firstName?.message}>
                <input
                  id="profile-first-name"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.firstName ? true : undefined}
                  aria-describedby={errors.firstName ? 'profile-first-name-error' : undefined}
                  {...register('firstName', nameRule('First name'))}
                />
              </Field>
              <Field label="Last name" htmlFor="profile-last-name" error={errors.lastName?.message}>
                <input
                  id="profile-last-name"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.lastName ? true : undefined}
                  aria-describedby={errors.lastName ? 'profile-last-name-error' : undefined}
                  {...register('lastName', nameRule('Last name'))}
                />
              </Field>
            </div>
          </Card>

          <Card title="Health profile" description="Used to personalize your nutrition targets">
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Age" htmlFor="profile-age" error={errors.age?.message}>
                <input
                  id="profile-age"
                  type="number"
                  inputMode="numeric"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.age ? true : undefined}
                  aria-describedby={errors.age ? 'profile-age-error' : undefined}
                  {...register('age', numberRule('Age', 10, 120))}
                />
              </Field>
              <Field label="Gender" htmlFor="profile-gender" error={errors.gender?.message}>
                <select
                  id="profile-gender"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.gender ? true : undefined}
                  aria-describedby={errors.gender ? 'profile-gender-error' : undefined}
                  {...register('gender', { required: 'Gender is required' })}
                >
                  <option value="">Select gender</option>
                  {GENDER_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="Height (cm)" htmlFor="profile-height" error={errors.height?.message}>
                <input
                  id="profile-height"
                  type="number"
                  inputMode="decimal"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.height ? true : undefined}
                  aria-describedby={errors.height ? 'profile-height-error' : undefined}
                  {...register('height', numberRule('Height', 50, 250))}
                />
              </Field>
              <Field label="Weight (kg)" htmlFor="profile-weight" error={errors.weight?.message}>
                <input
                  id="profile-weight"
                  type="number"
                  inputMode="decimal"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.weight ? true : undefined}
                  aria-describedby={errors.weight ? 'profile-weight-error' : undefined}
                  {...register('weight', numberRule('Weight', 20, 300))}
                />
              </Field>
              <Field label="Activity level" htmlFor="profile-activity" error={errors.activityLevel?.message}>
                <select
                  id="profile-activity"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.activityLevel ? true : undefined}
                  aria-describedby={errors.activityLevel ? 'profile-activity-error' : undefined}
                  {...register('activityLevel', { required: 'Activity level is required' })}
                >
                  <option value="">Select activity level</option>
                  {ACTIVITY_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="Goal" htmlFor="profile-goal" error={errors.goal?.message}>
                <select
                  id="profile-goal"
                  className={INPUT_CLASSES}
                  aria-invalid={errors.goal ? true : undefined}
                  aria-describedby={errors.goal ? 'profile-goal-error' : undefined}
                  {...register('goal', { required: 'Goal is required' })}
                >
                  <option value="">Select goal</option>
                  {GOAL_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </Field>
            </div>
          </Card>

          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={cancelEdit} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button type="submit" isLoading={isSubmitting}>
              <Save aria-hidden="true" className="h-4 w-4" />
              Save changes
            </Button>
          </div>
        </form>
      ) : (
        <div className="mt-6 grid gap-4 lg:grid-cols-2">
          <Card title="Account" description="Your account details">
            <dl className="space-y-3 text-sm">
              <div>
                <dt className="text-xs text-muted-foreground">Name</dt>
                <dd className="mt-0.5 font-medium text-foreground">
                  {user.firstName} {user.lastName}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">Email</dt>
                <dd className="mt-0.5 font-medium text-foreground">{user.email}</dd>
              </div>
            </dl>
          </Card>

          <Card title="Health profile" description="The data driving your plan">
            <dl className="grid grid-cols-2 gap-4">
              {healthRows.map((row) => (
                <div key={row.label}>
                  <dt className="text-xs text-muted-foreground">{row.label}</dt>
                  <dd className="mt-0.5 text-sm font-semibold text-foreground">
                    {row.value}
                  </dd>
                </div>
              ))}
            </dl>
          </Card>
        </div>
      )}
    </main>
  )
}

