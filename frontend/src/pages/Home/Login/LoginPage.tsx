import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'

import { Eye, EyeOff, Leaf } from 'lucide-react'
import { useAuth } from '../../../hooks/useAuth'
import { routePaths } from '../../../routes/routePaths'
import type { LoginRequest } from '../../../types/auth'
import { Field, INPUT_CLASSES } from '../../../components/ui/Field'

interface LoginFormValues {
  email: string
  password: string
}

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
              <Leaf className="h-5 w-5" />
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
          <ul className="mt-8 space-y-3 text-sm text-muted-foreground">
            <li className="flex items-center gap-2.5">
              <span className="h-1.5 w-1.5 rounded-full bg-primary" />
              Your profile and goals, ready
            </li>
            <li className="flex items-center gap-2.5">
              <span className="h-1.5 w-1.5 rounded-full bg-primary" />
              Fresh recommendations as you update them
            </li>
            <li className="flex items-center gap-2.5">
              <span className="h-1.5 w-1.5 rounded-full bg-primary" />
              Secure, private health data
            </li>
          </ul>
        </div>

        {/* Form card */}
        <div className="w-full">
          <div className="rounded-2xl border border-border bg-card p-6 shadow-sm sm:p-8">
            <h1 className="text-2xl font-semibold tracking-tight">Sign in</h1>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Enter your email and password to continue.
            </p>

            {registered && (
              <Alert tone="success" className="mt-5">
                Registration successful. Please sign in.
              </Alert>
            )}

            {serverError && (
              <Alert tone="error" className="mt-5">
                {serverError}
              </Alert>
            )}

            <form onSubmit={handleSubmit(onSubmit)} noValidate className="mt-6 space-y-4">
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
                      message:
                       'Enter a valid email address',
                    },
                  })}
                />
              </Field>

              <Field label="Password" htmlFor="password" error={errors.password?.message}>
                <div className="relative">
                  <input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
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

              <Button type="submit" size="lg" className="w-full" isLoading={isSubmitting}>
                Sign in
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

function getErrorMessage(error: unknown): import("react").SetStateAction<string | null> {
  throw new Error('Function not implemented.')
}
