import { Link } from 'react-router-dom'

import { Home as HomeIcon } from 'lucide-react'
import { buttonClassName } from '../../../../../../../../../../../../components/ui/Button'
import { routePaths } from '../../../../../../../../../../../../routes/routePaths'


/** 404 — unknown URLs land here with working escapes. */
export function NotFoundPage() {
  return (
    <main className="mx-auto flex min-h-[60vh] max-w-md flex-col items-center justify-center px-4 py-12 text-center sm:px-6 lg:px-8">
      <p className="text-6xl font-bold tracking-tight text-primary">404</p>
      <h1 className="mt-4 text-2xl font-bold tracking-tight">Page not found</h1>
      <p className="mt-2 text-muted-foreground">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <div className="mt-8 flex flex-col gap-2 sm:flex-row">
        <Link to={routePaths.home} className={buttonClassName('primary', 'md')}>
          <HomeIcon aria-hidden="true" className="h-4 w-4" />
          Back to home
        </Link>
        <Link to={routePaths.foods} className={buttonClassName('outline', 'md')}>
          Browse foods
        </Link>
      </div>
    </main>
  )
}