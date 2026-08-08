import { Link } from 'react-router-dom'

import { Leaf } from 'lucide-react'

import { useAuth } from '../../hooks/useAuth'
import { routePaths } from '../../routes/routePaths'

const PRODUCT_LINKS = [
  { to: routePaths.foods, label: 'Foods' },
  { to: routePaths.about, label: 'About' },
  { to: routePaths.contact, label: 'Contact' },
] as const

const COMPANY_LINKS = [
  { to: routePaths.about, label: 'About' },
  { to: routePaths.contact, label: 'Contact' },
] as const

const LEGAL_LINKS = [
  { to: routePaths.home, label: 'Privacy Policy' },
  { to: routePaths.home, label: 'Terms of Service' },
] as const

/**
 * Global footer.
 *
 * Matches the Navbar's design language (flat background, plain logo).
 * The CTA is auth-aware: signed-out visitors get "Get started",
 * signed-in visitors get "Go to dashboard".
 */
export function Footer() {
  const { isAuthenticated } = useAuth()

  return (
    <footer className="border-t border-border bg-background">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="flex flex-col gap-10 md:flex-row md:items-start md:justify-between">
          {/* Brand */}
          <div className="max-w-sm">
            <Link
              to={routePaths.home}
              className="flex items-center gap-2 font-semibold text-foreground"
            >
              <Leaf className="h-5 w-5 text-primary" />
              <span>NutriGuideAI</span>
            </Link>
            <p className="mt-3 text-sm text-muted-foreground">
              Personalized, evidence-informed nutrition guidance built
              around your health goals.
            </p>
            <Link
              to={
                isAuthenticated ? routePaths.dashboard : routePaths.register
              }
              className="mt-4 inline-flex rounded-md bg-primary px-3.5 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
            >
              {isAuthenticated ? 'Go to dashboard' : 'Get started'}
            </Link>
          </div>

          {/* Link columns */}
          <div className="grid grid-cols-2 gap-10 sm:grid-cols-3">
            <div>
              <h3 className="text-sm font-semibold">Product</h3>
              <ul className="mt-3 space-y-2">
                {PRODUCT_LINKS.map((link) => (
                  <li key={link.label}>
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

            <div>
              <h3 className="text-sm font-semibold">Company</h3>
              <ul className="mt-3 space-y-2">
                {COMPANY_LINKS.map((link) => (
                  <li key={link.label}>
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

            <div>
              <h3 className="text-sm font-semibold">Legal</h3>
              <ul className="mt-3 space-y-2">
                {LEGAL_LINKS.map((link) => (
                  <li key={link.label}>
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
        </div>

        <div className="mt-10 border-t border-border pt-6">
          <p className="text-sm text-muted-foreground">
            © {new Date().getFullYear()} NutriGuide AI. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  )
}