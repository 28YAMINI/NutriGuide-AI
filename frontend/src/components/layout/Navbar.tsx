import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'

import { useAuth } from '../../hooks/useAuth'
import { useTheme } from '../../hooks/useTheme'
import { routePaths } from '../../routes/routePaths'
import {
  Leaf as LeafIcon,
  Menu as MenuIcon,
  Moon as MoonIcon,
  Sun as SunIcon,
  X as CloseIcon,
} from 'lucide-react'

const NAV_LINKS = [
  { to: routePaths.home, label: 'Home' },
  { to: routePaths.foods, label: 'Foods' },
  { to: routePaths.about, label: 'About' },
  { to: routePaths.contact, label: 'Contact' },
] as const

/**
 * Global top navigation.
 *
 * Session-aware: shows auth actions (Sign in / Get started) when signed
 * out, and Dashboard / Admin / avatar / Logout when signed in. The
 * theme toggle lives here so the whole chrome stays in one component.
 */
export function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  // Close the mobile menu whenever the route changes.
  useEffect(() => {
    setIsMenuOpen(false)
  }, [location.pathname])

  const isDark = theme === 'dark'

  const handleLogout = () => {
    logout()
    navigate(routePaths.home)
  }

  const initials = user
    ? `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase()
    : ''

  return (
    <header className="border-b border-border bg-background">
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link
          to={routePaths.home}
          className="flex items-center gap-2 font-semibold text-foreground"
        >
          <LeafIcon className="h-5 w-5 text-primary" />
          <span>NutriGuideAI</span>
        </Link>

        {/* Desktop nav links */}
        <div className="hidden items-center gap-1 md:flex">
          {NAV_LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                  isActive
                    ? 'text-foreground'
                    : 'text-muted-foreground hover:text-foreground'
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </div>

        {/* Right-side actions */}
        <div className="flex items-center gap-2">
          {/* Theme toggle */}
          <button
            type="button"
            onClick={toggleTheme}
            aria-label={
              isDark ? 'Switch to light mode' : 'Switch to dark mode'
            }
            className="rounded-md p-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          >
            {isDark ? <SunIcon /> : <MoonIcon />}
          </button>

          {/* Desktop auth actions */}
          <div className="hidden items-center gap-2 md:flex">
            {isAuthenticated ? (
              <>
                {isAdmin && (
                  <NavLink
                    to={routePaths.admin}
                    className="rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
                  >
                    Admin
                  </NavLink>
                )}

                <NavLink
                  to={routePaths.dashboard}
                  className="rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
                >
                  Dashboard
                </NavLink>

                <Link
                  to={routePaths.dashboard}
                  title={`Signed in as ${user?.firstName} ${user?.lastName}`}
                  className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary transition-colors hover:bg-primary/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                >
                  {initials}
                </Link>

                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to={routePaths.login}
                  className="rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
                >
                  Sign in
                </Link>

                <Link
                  to={routePaths.register}
                  className="rounded-md bg-primary px-3.5 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                >
                  Get started
                </Link>
              </>
            )}
          </div>

          {/* Mobile menu toggle */}
          <button
            type="button"
            onClick={() => setIsMenuOpen((open) => !open)}
            aria-label={isMenuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={isMenuOpen}
            aria-controls="mobile-menu"
            className="rounded-md p-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary md:hidden"
          >
            {isMenuOpen ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>
      </nav>

      {/* Mobile menu */}
      {isMenuOpen && (
        <div id="mobile-menu" className="border-t border-border md:hidden">
          <div className="mx-auto max-w-7xl space-y-1 px-4 py-3 sm:px-6 lg:px-8">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `block rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                    isActive
                      ? 'bg-muted text-foreground'
                      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                  }`
                }
              >
                {link.label}
              </NavLink>
            ))}

            {isAuthenticated && isAdmin && (
              <NavLink
                to={routePaths.admin}
                className="block rounded-md px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-muted hover:text-foreground"
              >
                Admin
              </NavLink>
            )}

            {isAuthenticated && (
              <NavLink
                to={routePaths.dashboard}
                className="block rounded-md px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-muted hover:text-foreground"
              >
                Dashboard
              </NavLink>
            )}

            <div className="flex flex-col gap-2 pt-2">
              {isAuthenticated ? (
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-md px-3 py-2 text-left text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                >
                  Logout
                </button>
              ) : (
                <>
                  <Link
                    to={routePaths.login}
                    className="rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                  >
                    Sign in
                  </Link>

                  <Link
                    to={routePaths.register}
                    className="rounded-md bg-primary px-3 py-2 text-center text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
                  >
                    Get started
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </header>
  )
}