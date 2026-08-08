import { Link } from 'react-router-dom'

import { Compass } from 'lucide-react'
import { routePaths } from '../../../../../../../../../../../../routes/routePaths'



/**
 * 404 page — rendered by the catch-all route for any unmatched path.
 */
export function NotFoundPage() {
  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center px-4 py-12">
      <div className="mx-auto max-w-md text-center">
        <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
          <Compass aria-hidden="true" className="h-6 w-6" />
        </span>
        <p className="mt-6 text-6xl font-bold tracking-tight text-primary">404</p>
        <h1 className="mt-3 text-2xl font-bold tracking-tight">Page not found</h1>
        <p className="mt-2 text-muted-foreground">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <Link
            to={routePaths.home}
            className="inline-flex items-center justify-center rounded-lg bg-primary px-5 py-2.5 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90"
          >
            Back to home
          </Link>
          <Link
            to={routePaths.foods}
            className="inline-flex items-center justify-center rounded-lg border border-border bg-background px-5 py-2.5 text-sm font-medium text-foreground transition-colors hover:bg-muted"
          >
            Browse foods
          </Link>
        </div>
      </div>
    </div>
  )
}