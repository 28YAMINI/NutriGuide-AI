import { useEffect, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'

import {
  Calendar,
  BookOpen,
  BarChart3,
  Leaf as LeafIcon,
  Menu as MenuIcon,
  Moon as MoonIcon,
  Sun as SunIcon,
  X as CloseIcon,
} from 'lucide-react'

import { useAuth } from '../../hooks/useAuth'
import { useTheme } from '../../hooks/useTheme'
import { routePaths } from '../../routes/routePaths'

const NAV_LINKS = [
  { to: routePaths.home, label: 'Home' },
  { to: routePaths.foods, label: 'Foods' },
  { to: routePaths.about, label: 'About' },
  { to: routePaths.contact, label: 'Contact' },
] as const

const PROTECTED_LINKS = [
  { to: routePaths.dashboard, label: 'Dashboard', icon: null },
  { to: routePaths.mealPlan, label: 'Meal Plan', icon: Calendar },
  { to: routePaths.foodDiary, label: 'Food Diary', icon: BookOpen },
  { to: routePaths.progress, label: 'Progress', icon: BarChart3 },
] as const
const BASE_NAV_CLASS =
  'cursor-pointer rounded-md px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'

const desktopNavClass = ({ isActive }: { isActive: boolean }) =>
  `${BASE_NAV_CLASS} ${
    isActive
      ? 'bg-muted text-foreground'
      : 'text-muted-foreground hover:bg-muted hover:text-foreground'
  }`

const mobileNavClass =
  'block rounded-md px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'

const iconButtonClass =
  'cursor-pointer rounded-md p-2 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'

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
    <header className="sticky top-0 z-40 border-b border-border bg-background/80 backdrop-blur">
      <nav
        aria-label="Main navigation"
        className="mx-auto flex h-16 max-w-7xl items-center justify-between gap-4 px-4 sm:px-6 lg:px-8"
      >
        {/* Logo */}
        <Link
          to={routePaths.home}
          className="flex items-center gap-2.5 rounded-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
            <LeafIcon aria-hidden="true" className="h-4 w-4" />
          </span>
          <span className="text-base font-semibold tracking-tight">
            NutriGuide<span className="text-primary">AI</span>
          </span>
        </Link>

        {/* Desktop nav links */}
        <div className="hidden items-center gap-1 md:flex">
          {NAV_LINKS.map((link) => (
              <NavLink key={link.to} to={link.to} className={desktopNavClass}>
                {link.label}
              </NavLink>
          ))}
          {isAuthenticated &&
              PROTECTED_LINKS.map((link) => (
                  <NavLink key={link.to} to={link.to} className={desktopNavClass}>
                    {link.label}
                  </NavLink>
              ))}
        </div>

        {/* Right-side actions */}
        <div className="flex items-center gap-1.5">
          {/* Theme toggle */}
          <button
            type="button"
            onClick={toggleTheme}
            aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
            className={iconButtonClass}
          >
            {isDark ? (
              <SunIcon aria-hidden="true" className="h-4 w-4" />
            ) : (
              <MoonIcon aria-hidden="true" className="h-4 w-4" />
            )}
          </button>

          {/* Desktop auth actions */}
          <div className="hidden items-center gap-1.5 md:flex">
            {isAuthenticated ? (
                    <>
                    {isAdmin && (
                        <NavLink to={routePaths.admin} className={desktopNavClass}>
                          Admin
                        </NavLink>
                    )}

                    <Link

                  to={routePaths.dashboard}
                  title={`Signed in as ${user?.firstName} ${user?.lastName}`}
                  className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary transition-colors hover:bg-primary/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  {initials}
                </Link>

                <button
                  type="button"
                  onClick={handleLogout}
                  className={`${BASE_NAV_CLASS} cursor-pointer text-muted-foreground hover:bg-muted hover:text-foreground`}
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to={routePaths.login}
                  className={`${BASE_NAV_CLASS} text-muted-foreground hover:bg-muted hover:text-foreground`}
                >
                  Sign in
                </Link>

                <Link
                  to={routePaths.register}
                  className="cursor-pointer rounded-lg bg-primary px-3.5 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background"
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
            className={`${iconButtonClass} md:hidden`}
          >
            {isMenuOpen ? (
              <CloseIcon aria-hidden="true" className="h-4 w-4" />
            ) : (
              <MenuIcon aria-hidden="true" className="h-4 w-4" />
            )}
          </button>
        </div>
      </nav>

      {/* Mobile menu */}
      {isMenuOpen && (
        <div id="mobile-menu" className="border-t border-border bg-background md:hidden">
          <nav
            aria-label="Mobile navigation"
            className="mx-auto max-w-7xl space-y-1 px-4 py-3 sm:px-6 lg:px-8"
          >
            {NAV_LINKS.map((link) => (
                <NavLink
                    key={link.to}
                    to={link.to}
                    className={({ isActive }) =>
                        `${mobileNavClass} ${
                            isActive ? 'bg-muted text-foreground' : ''
                        }`
                    }
                >
                  {link.label}
                </NavLink>
            ))}

            {isAuthenticated &&
                PROTECTED_LINKS.map((link) => (
                    <NavLink
                        key={link.to}
                        to={link.to}
                        className={({ isActive }) =>
                            `${mobileNavClass} ${
                                isActive ? 'bg-muted text-foreground' : ''
                            }`
                        }
                    >
                  <span className="flex items-center gap-2">
                    {link.icon && <link.icon aria-hidden="true" className="h-4 w-4" />}
                    {link.label}
                  </span>
                    </NavLink>
                ))}

            {isAuthenticated && isAdmin && (
                <NavLink to={routePaths.admin} className={mobileNavClass}>
                  Admin
                </NavLink>
            )}
            <div className="flex flex-col gap-2 border-t border-border pt-3">
              {isAuthenticated ? (
                <button
                  type="button"
                  onClick={handleLogout}
                  className={`${mobileNavClass} w-full cursor-pointer text-left`}
                >
                  Logout
                </button>
              ) : (
                <>
                  <Link to={routePaths.login} className={mobileNavClass}>
                    Sign in
                  </Link>

                  <Link
                    to={routePaths.register}
                    className="cursor-pointer rounded-md bg-primary px-3 py-2 text-center text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                  >
                    Get started
                  </Link>
                </>
              )}
            </div>
          </nav>
        </div>
      )}
    </header>
  )
}