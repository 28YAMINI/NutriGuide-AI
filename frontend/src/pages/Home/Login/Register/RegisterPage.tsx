import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'

import { Eye, EyeOff, Leaf } from 'lucide-react'
import type { ActivityLevel, Gender, Goal } from '../../../../types/user'
import { useAuth } from '../../../../hooks/useAuth'
import type { RegisterRequest } from '../../../../types/auth'
import { routePaths } from '../../../../routes/routePaths'
import { Field, INPUT_CLASSES } from '../../../../components/ui/Field'
import { Spinner } from '../../../../components/common/Spinner'



interface RegisterFormValues {
  firstName: string
  lastName: string
  email: string
  password: string
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

/**
 * Registration page.
 *
 * Collects the account fields plus the health profile the backend
 * RegisterRequest expects. The backend intentionally issues no token
 * on registration, so a successful submit redirects to /login with a
 * success notice instead of auto-signing the user in.
 */
export function RegisterPage() {
  const { register: registerUser } = useAuth()
  const navigate = useNavigate()

  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      age: '',
      gender: '',
      height: '',
      weight: '',
      activityLevel: '',
      goal: '',
    },
  })

  const onSubmit = async (values: RegisterFormValues) => {
    setServerError(null)

    const payload: RegisterRequest = {
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      email: values.email.trim().toLowerCase(),
      password: values.password,
      age: Number(values.age),
      gender: values.gender as Gender,
      height: Number(values.height),
      weight: Number(values.weight),
      activityLevel: values.activityLevel as ActivityLevel,
      goal: values.goal as Goal,
    }

    try {
      await registerUser(payload)
      navigate(`${routePaths.login}?registered=1`, { replace: true })
    } catch (error) {
      setServerError(getErrorMessage(error))
    }
  }

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
      <div className="grid w-full max-w-5xl gap-12 lg:grid-cols-[1fr_1.3fr] lg:items-center">
        {/* Brand panel (desktop only) */}
        <div className="hidden lg:block">
          <div className="flex items-center gap-2.5">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary text-primary-foreground">
              <Leaf className="h-5 w-5" />
            </span>
            <span className="text-lg font-semibold tracking-tight">
              NutriGuide<span className="text-primary">AI</span>
            </span>
          </div>
          <h1 className="mt-8 text-3xl font-bold leading-tight tracking-tight">
            A plan built around you — not around a trend.
          </h1>
          <p className="mt-4 text-muted-foreground">
            Tell us about your body, activity and goals. Your personalized
            nutrition plan starts from these details.
          </p>
          <ul className="mt-8 space-y-3 text-sm text-muted-foreground">
            <li className="flex items-center gap-2.5">
              <span className="h-1.5 w-1.5 rounded-full bg-primary" />
              Evidence-informed recommendations
            </li>
            <li className="flex items-center gap-2.5">
              <span className="h-1.5 w-1.5 rounded-full bg-primary" />
              Budget and preference aware
            </li>
            <li className="flex items-center gap-2.5">
              <span className="h-1.5 w-1.5 rounded-full bg-primary" />
              Your data, yours only
            </li>
          </ul>
        </div>

        {/* Form card */}
        <div className="w-full">
          <div className="rounded-2xl border border-border bg-card p-6 shadow-sm sm:p-8">
            <h1 className="text-2xl font-semibold tracking-tight">Create your account</h1>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Free forever. No credit card required.
            </p>

            {serverError ? (
              <div
                role="alert"
                className="mt-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-400"
              >
                {serverError}
              </div>
            ) : null}

            <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-6 space-y-6">
              <section className="space-y-4">
                <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Account
                </h2>

                <div className="grid gap-4 sm:grid-cols-2">
                  <Field label="First name" htmlFor="firstName" error={errors.firstName?.message}>
                    <input
                      id="firstName"
                      type="text"
                      autoComplete="given-name"
                      className={INPUT_CLASSES}
                      {...register('firstName', {
                        required: 'First name is required',
                        minLength: { value: 2, message: 'First name must be at least 2 characters' },
                        maxLength: { value: 50, message: 'First name must be at most 50 characters' },
                      })}
                    />
                  </Field>

                  <Field label="Last name" htmlFor="lastName" error={errors.lastName?.message}>
                    <input
                      id="lastName"
                      type="text"
                      autoComplete="family-name"
                      className={INPUT_CLASSES}
                      {...register('lastName', {
                        required: 'Last name is required',
                        minLength: { value: 2, message: 'Last name must be at least 2 characters' },
                        maxLength: { value: 50, message: 'Last name must be at most 50 characters' },
                      })}
                    />
                  </Field>
                </div>

                <Field label="Email" htmlFor="email" error={errors.email?.message}>
                  <input
                    id="email"
                    type="email"
                    autoComplete="email"
                    placeholder="you@example.com"
                    className={INPUT_CLASSES}
                    {...register('email', {
                      required: 'Email is required',
                      pattern: {
                        value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                        message: 'Enter a valid email address',
                      },
                    })}
                  />
                </Field>

                <Field label="Password" htmlFor="password" error={errors.password?.message}>
                  <div className="relative">
                    <input
                      id="password"
                      type={showPassword ? 'text' : 'password'}
                      autoComplete="new-password"
                      placeholder="At least 8 characters"
                      className={`${INPUT_CLASSES} pr-10`}
                      {...register('password', {
                        required: 'Password is required',
                        minLength: { value: 8, message: 'Password must be at least 8 characters' },
                      })}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((show) => !show)}
                      aria-label={showPassword ? 'Hide password' : 'Show password'}
                      className="absolute inset-y-0 right-0 flex items-center pr-3 text-muted-foreground transition-colors hover:text-foreground"
                    >
                      {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </Field>
              </section>

              <div className="border-t border-border" />

              <section className="space-y-4">
                <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  Health profile
                </h2>

                <div className="grid gap-4 sm:grid-cols-3">
                  <Field label="Age" htmlFor="age" error={errors.age?.message}>
                    <input
                      id="age"
                      type="number"
                      inputMode="numeric"
                      min={13}
                      max={120}
                      placeholder="e.g. 24"
                      className={INPUT_CLASSES}
                      {...register('age', {
                        required: 'Age is required',
                        min: { value: 13, message: 'Age must be between 13 and 120' },
                        max: { value: 120, message: 'Age must be between 13 and 120' },
                      })}
                    />
                  </Field>

                  <Field label="Height (cm)" htmlFor="height" error={errors.height?.message}>
                    <input
                      id="height"
                      type="number"
                      inputMode="decimal"
                      min={50}
                      max={250}
                      placeholder="e.g. 170"
                      className={INPUT_CLASSES}
                      {...register('height', {
                        required: 'Height is required',
                        min: { value: 50, message: 'Height must be between 50 and 250 cm' },
                        max: { value: 250, message: 'Height must be between 50 and 250 cm' },
                      })}
                    />
                  </Field>

                  <Field label="Weight (kg)" htmlFor="weight" error={errors.weight?.message}>
                    <input
                      id="weight"
                      type="number"
                      inputMode="decimal"
                      min={20}
                      max={300}
                      placeholder="e.g. 65"
                      className={INPUT_CLASSES}
                      {...register('weight', {
                        required: 'Weight is required',
                        min: { value: 20, message: 'Weight must be between 20 and 300 kg' },
                        max: { value: 300, message: 'Weight must be between 20 and 300 kg' },
                      })}
                    />
                  </Field>
                </div>

                <Field label="Gender" htmlFor="gender" error={errors.gender?.message}>
                  <select
                    id="gender"
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
                  htmlFor="activityLevel"
                  error={errors.activityLevel?.message}
                >
                  <select
                    id="activityLevel"
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

                <Field label="Goal" htmlFor="goal" error={errors.goal?.message}>
                  <select
                    id="goal"
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
              </section>

              <button
                type="submit"
                disabled={isSubmitting}
                className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isSubmitting ? (
                  <>
                    <Spinner size="sm" />
                    Creating account…
                  </>
                ) : (
                  'Create account'
                )}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-muted-foreground">
              Already have an account?{' '}
              <Link
                to={routePaths.login}
                className="font-medium text-primary transition-colors hover:underline"
              >
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

function getErrorMessage(error: unknown): import("react").SetStateAction<string | null> {
    throw new Error('Function not implemented.')
}
