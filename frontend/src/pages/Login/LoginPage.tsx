import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import axios from 'axios'

import { Eye, EyeOff, Leaf } from 'lucide-react'

import { useAuth } from '../../hooks/useAuth.ts'
import { routePaths } from '../../routes/routePaths.ts'
import type { LoginRequest } from '../../types/auth.ts'
import { Field, INPUT_CLASSES } from '../../components/ui/Field.tsx'
import { Button } from '../../components/ui/Button.tsx'
import { Alert } from '../../components/ui/Alert.tsx'

interface LoginFormValues {
  email: string
  password: string
}

const TRUST_POINTS = [
  'Your profile and goals, ready',
  'Fresh recommendations as you update them',
  'Secure, private health data',
] as const

/**
 * Sign-in page.
 *
 * Reads two query params written by other parts of the app:
 * ?registered=1 (success notice after registration) and
 * ?returnTo=... (destination preserved by ProtectedRoute).
 */
export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()

  const returnTo = searchParams.get('returnTo')
  const registered = searchParams.get('registered') === '1'

  const [showPassword, setShowPassword] = useState(false)
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    defaultValues: { email: '', password: '' },
  })

  const onSubmit = async (values: LoginFormValues) => {
    setServerError(null)

    const payload: LoginRequest = {
      email: values.email.trim().toLowerCase(),
      password: values.password,
    }

    try {
      await login(payload)
      navigate(returnTo ?? routePaths.dashboard, { replace: true })
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
              <Leaf aria-hidden="true" className="h-5 w-5" />
            </span>
            <span className="text-lg font-semibold tracking-tight">
              NutriGuide<span className="text-primary">AI</span>
            </span>
          </div>
          <h1 className="mt-8 text-3xl font-bold leading-tight tracking-tight">
            Welcome back to your plan.
          </h1>
          <p className="mt-4 text-muted-foreground">
            Sign in to pick up your personalized nutrition guidance exactly
            where you left off.
          </p>
          <ul className="mt-8 space-y-3.5 text-sm text-muted-foreground">
            {TRUST_POINTS.map((point) => (
              <li key={point} className="flex items-center gap-2.5">
                <span
                  aria-hidden="true"
                  className="h-1.5 w-1.5 shrink-0 rounded-full bg-primary"
                />
                {point}
              </li>
            ))}
          </ul>
        </div>

        {/* Form card */}
        <div className="w-full">
          <div className="rounded-2xl border border-border bg-card p-6 shadow-sm sm:p-8">
            <h1 className="text-2xl font-semibold tracking-tight">Sign in</h1>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Enter your email and password to continue.
            </p>

            {registered ? (
              <Alert tone="success" className="mt-5">
                Registration successful. Please sign in.
              </Alert>
            ) : null}

            {serverError ? (
              <Alert
                tone="error"
                className="mt-5"
                onDismiss={() => setServerError(null)}
              >
                {serverError}
              </Alert>
            ) : null}

            <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-6 space-y-5">
              <Field
                label="Email"
                htmlFor="email"
                error={errors.email?.message}
                required
              >
                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  placeholder="you@example.com"
                  aria-invalid={errors.email ? true : undefined}
                  aria-describedby={errors.email ? 'email-error' : undefined}
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

              <Field
                label="Password"
                htmlFor="password"
                error={errors.password?.message}
                required
              >
                <div className="relative">
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    aria-invalid={errors.password ? true : undefined}
                    aria-describedby={errors.password ? 'password-error' : undefined}
                    className={`${INPUT_CLASSES} pr-10`}
                    {...register('password', {
                      required: 'Password is required',
                      minLength: {
                        value: 8,
                        message: 'Password must be at least 8 characters',
                      },
                    })}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword((show) => !show)}
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                    className="absolute inset-y-0 right-0 flex cursor-pointer items-center pr-3 text-muted-foreground transition-colors hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  >
                    {showPassword ? (
                      <EyeOff aria-hidden="true" className="h-4 w-4" />
                    ) : (
                      <Eye aria-hidden="true" className="h-4 w-4" />
                    )}
                  </button>
                </div>
              </Field>

              <Button
                type="submit"
                size="lg"
                className="w-full"
                isLoading={isSubmitting}
              >
                {isSubmitting ? 'Signing in…' : 'Sign in'}
              </Button>
            </form>

            <p className="mt-6 text-center text-sm text-muted-foreground">
              Don't have an account?{' '}
              <Link
                to={routePaths.register}
                className="font-medium text-primary transition-colors hover:underline"
              >
                Create one
              </Link>
            </p>
          </div>
        </div>
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