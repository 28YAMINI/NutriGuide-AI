import { Link } from 'react-router-dom'

import { Compass, Home, Utensils } from 'lucide-react'
import { buttonClassName } from '../../components/ui/Button.tsx'
import { routePaths } from '../../routes/routePaths.ts'


/**
 * 404 page.
 *
 * Shown for unknown routes. Offers a clear way back home and into
 * the food catalog so the user never hits a dead end.
 */
export function NotFoundPage() {
  return (
    <main className="flex min-h-[calc(100vh-4rem)] items-center justify-center px-6 py-16">
      <div className="mx-auto max-w-md text-center">
        <span className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-primary/10 text-primary">
          <Compass aria-hidden="true" className="h-7 w-7" />
        </span>

        <p className="mt-6 text-6xl font-bold tracking-tight text-primary sm:text-7xl">
          404
        </p>

        <h1 className="mt-2 text-2xl font-semibold tracking-tight text-foreground">
          Page not found
        </h1>

        <p className="mt-3 text-muted-foreground">
          The page you're looking for doesn't exist or has been moved.
        </p>

        <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Link
            to={routePaths.home}
            className={buttonClassName('primary', 'md', 'w-full sm:w-auto')}
          >
            <Home aria-hidden="true" className="h-4 w-4" />
            Back to home
          </Link>
          <Link
            to={routePaths.foods}
            className={buttonClassName('outline', 'md', 'w-full sm:w-auto')}
          >
            <Utensils aria-hidden="true" className="h-4 w-4" />
            Browse foods
          </Link>
        </div>
      </div>
    </main>
  )
}