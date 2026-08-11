import { Link } from 'react-router-dom'

import { Leaf as LeafIcon } from 'lucide-react'

import { routePaths } from '../../routes/routePaths'

const PRODUCT_LINKS = [
  { to: routePaths.home, label: 'Home' },
  { to: routePaths.foods, label: 'Foods' },
  { to: routePaths.about, label: 'About' },
  { to: routePaths.contact, label: 'Contact' },
] as const

const ACCOUNT_LINKS = [
  { to: routePaths.login, label: 'Sign in' },
  { to: routePaths.register, label: 'Create account' },
  { to: routePaths.dashboard, label: 'Dashboard' },
  { to: routePaths.profile, label: 'Profile' },
] as const

const linkClass =
  'text-sm text-muted-foreground transition-colors hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm'

/**
 * Global footer.
 *
 * Brand block, product and account navigation, and the medical
 * disclaimer. All links stay client-side via React Router.
 */
export function Footer() {
  const year = new Date().getFullYear()

  return (
    <footer className="border-t border-border bg-muted/30">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid gap-10 md:grid-cols-[1.5fr_1fr_1fr]">
          {/* Brand */}
          <div>
            <Link
              to={routePaths.home}
              className="inline-flex items-center gap-2.5 font-semibold text-foreground"
            >
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
                <LeafIcon aria-hidden="true" className="h-4 w-4" />
              </span>
              <span>
                NutriGuide<span className="text-primary">AI</span>
              </span>
            </Link>
            <p className="mt-3 max-w-sm text-sm text-muted-foreground">
              Personalized nutrition guidance built around your body, your
              goals, and your budget — with your privacy respected.
            </p>
          </div>

          {/* Product */}
          <nav aria-label="Product">
            <h2 className="text-sm font-semibold text-foreground">Product</h2>
            <ul className="mt-3 space-y-2.5">
              {PRODUCT_LINKS.map((link) => (
                <li key={link.to}>
                  <Link to={link.to} className={linkClass}>
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>

          {/* Account */}
          <nav aria-label="Account">
            <h2 className="text-sm font-semibold text-foreground">Account</h2>
            <ul className="mt-3 space-y-2.5">
              {ACCOUNT_LINKS.map((link) => (
                <li key={link.to}>
                  <Link to={link.to} className={linkClass}>
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>
        </div>

        <div className="mt-10 space-y-1.5 border-t border-border pt-6">
          <p className="text-xs text-muted-foreground">
            © {year} NutriGuide AI. All rights reserved.
          </p>
          <p className="text-xs text-muted-foreground/80">
            NutriGuide AI provides general nutrition information and is not a
            substitute for professional medical advice.
          </p>
        </div>
      </div>
    </footer>
  )
}