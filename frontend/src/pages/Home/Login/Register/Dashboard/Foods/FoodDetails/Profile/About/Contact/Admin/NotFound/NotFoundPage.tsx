import { Link } from 'react-router-dom'
import { routePaths } from '../../../../../../../../../../../../routes/routePaths'



/**
 * 404 fallback page, rendered by the catch-all route.
 *
 * A minimal centered empty state with a single action:
 * return to the home page.
 */
export default function NotFoundPage() {
  return (
    <section className="flex min-h-screen items-center justify-center px-6">
      <div className="mx-auto max-w-md text-center">
        <p className="text-xs font-medium uppercase tracking-[0.2em] text-muted-foreground">
          404
        </p>
        <h1 className="mt-4 text-3xl font-semibold tracking-tight text-foreground sm:text-4xl">
          Page not found
        </h1>
        <p className="mt-3 text-sm leading-relaxed text-muted-foreground">
          The page you&apos;re looking for doesn&apos;t exist or has been moved.
        </p>
        <Link
          to={routePaths.home}
          className="mt-8 inline-flex items-center justify-center rounded-lg bg-primary-600 px-5 py-2.5 text-sm font-medium text-white transition-colors hover:bg-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
        >
          Back to home
        </Link>
      </div>
    </section>
  )
}