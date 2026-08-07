import { Link } from 'react-router-dom'


import { useAuth } from '../../hooks/useAuth'
import { routePaths } from '../../routes/routePaths'
import { LeafIcon } from 'lucide-react'

const PRODUCT_LINKS = [
  { to: routePaths.foods, label: 'Foods' },
  { to: routePaths.about, label: 'About' },
  { to: routePaths.contact, label: 'Contact' },
] as const

const COMPANY_LINKS = [
  { to: routePaths.home, label: 'Home' },
  { to: routePaths.login, label: 'Sign in' },
  { to: routePaths.register, label: 'Register' },
] as const

/**
 * Global footer.
 *
 * Only the CTA is auth-aware: signed-out visitors are pushed to
 * register, signed-in users are pointed back to the dashboard.
 */
export function Footer() {
  const { isAuthenticated } = useAuth()

  return (
    <footer className="border-t border-border bg-background">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid gap-10 sm:grid-cols-2 lg:grid-cols-4">
          {/* Brand */}
          <div className="lg:col-span-2">
            <Link
              to={routePaths.home}
              className="inline-flex items-center gap-2.5 rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
            >
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <LeafIcon className="h-4 w-4" />
              </span>
              <span className="text-base font-semibold tracking-tight">
                NutriGuide<span className="text-primary">AI</span>
              </span>
            </Link>

            <p className="mt-4 max-w-sm text-sm text-muted-foreground">
              Personalized meal plans built around your health profile,
              goals, lifestyle, and budget.
            </p>

            <div className="mt-6">
              {isAuthenticated ? (
                <Link
                  to={routePaths.dashboard}
                  className="inline-flex rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                >
                  Go to dashboard
                </Link>
              ) : (
                <Link
                  to={routePaths.register}
                  className="inline-flex rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                >
                  Get started — it&apos;s free
                </Link>
              )}
            </div>
          </div>

          {/* Product */}
          <div>
            <h2 className="text-sm font-semibold">Product</h2>
            <ul className="mt-4 space-y-3">
              {PRODUCT_LINKS.map((link) => (
                <li key={link.to}>
                  <Link
                    to={link.to}
                    className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Company */}
          <div>
            <h2 className="text-sm font-semibold">Company</h2>
            <ul className="mt-4 space-y-3">
              {COMPANY_LINKS.map((link) => (
                <li key={link.to}>
                  <Link
                    to={link.to}
                    className="text-sm text-muted-foreground transition-colors hover:text-foreground"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="mt-12 flex flex-col items-center justify-between gap-3 border-t border-border pt-6 sm:flex-row">
          <p className="text-xs text-muted-foreground">
            © {new Date().getFullYear()} NutriGuide AI. All rights reserved.
          </p>
          <p className="text-xs text-muted-foreground">
            Built for healthier habits.
          </p>
        </div>
      </div>
    </footer>
  )
}