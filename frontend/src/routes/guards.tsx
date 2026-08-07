import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

import { useAuth } from '../hooks/useAuth'
import { routePaths } from './routePaths'

interface GuardProps {
  children: ReactNode
}

/** Temporary placeholder — replaced by the shared Spinner component in Phase 3. */
function RouteSpinner() {
  return (
    <div className="flex min-h-screen items-center justify-center">
      <p className="text-sm text-muted-foreground">Loading...</p>
    </div>
  )
}

/**
 * Redirects already-authenticated users away from auth pages
 * (login/register) — prevents the "log in again" loop.
 */
export function PublicRoute({ children }: GuardProps) {
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <RouteSpinner />
  }

  if (isAuthenticated) {
    const returnTo = new URLSearchParams(location.search).get('returnTo')
    return <Navigate to={returnTo ?? routePaths.dashboard} replace />
  }

  return <>{children}</>
}

/**
 * Blocks signed-out users. Preserves the intended destination so the
 * Login page can return them there after a successful sign-in.
 */
export function ProtectedRoute({ children }: GuardProps) {
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <RouteSpinner />
  }

  if (!isAuthenticated) {
    const returnTo = encodeURIComponent(location.pathname + location.search)
    return <Navigate to={`${routePaths.login}?returnTo=${returnTo}`} replace />
  }

  return <>{children}</>
}

/**
 * Blocks non-admin users from admin-only pages.
 */
export function AdminRoute({ children }: GuardProps) {
  const { isAuthenticated, isAdmin, isLoading } = useAuth()
  const location = useLocation()

  if (isLoading) {
    return <RouteSpinner />
  }

  if (!isAuthenticated) {
    const returnTo = encodeURIComponent(location.pathname + location.search)
    return <Navigate to={`${routePaths.login}?returnTo=${returnTo}`} replace />
  }

  if (!isAdmin) {
    return <Navigate to={routePaths.dashboard} replace />
  }

  return <>{children}</>
}