import { Route, Routes } from 'react-router-dom'

import { MainLayout } from '../components/layout/MainLayout'
import { AdminRoute, ProtectedRoute, PublicRoute } from './guards'
import { routePaths } from './routePaths'

/**
 * Lightweight stand-in for the real page components (Phase 4).
 * Keeps routing wired and testable without blocking on page work.
 */
function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="flex min-h-[60vh] items-center justify-center px-6">
      <div className="mx-auto max-w-md text-center">
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          This page is under construction.
        </p>
      </div>
    </div>
  )
}

/**
 * Defines the application's route configuration.
 *
 * All routes render inside MainLayout, which provides the persistent
 * Navbar and Footer. Guards enforce access: PublicRoute for auth pages,
 * ProtectedRoute for signed-in pages, AdminRoute for ADMIN-only pages.
 */
export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        {/* Public */}
        <Route path={routePaths.home} element={<PlaceholderPage title="Home" />} />
        <Route path={routePaths.foods} element={<PlaceholderPage title="Foods" />} />
        <Route
          path={routePaths.foodDetails}
          element={<PlaceholderPage title="Food Details" />}
        />
        <Route path={routePaths.about} element={<PlaceholderPage title="About" />} />
        <Route
          path={routePaths.contact}
          element={<PlaceholderPage title="Contact" />}
        />
        <Route
          path={routePaths.login}
          element={
            <PublicRoute>
              <PlaceholderPage title="Login" />
            </PublicRoute>
          }
        />
        <Route
          path={routePaths.register}
          element={
            <PublicRoute>
              <PlaceholderPage title="Register" />
            </PublicRoute>
          }
        />

        {/* Protected */}
        <Route
          path={routePaths.dashboard}
          element={
            <ProtectedRoute>
              <PlaceholderPage title="Dashboard" />
            </ProtectedRoute>
          }
        />

        {/* Admin */}
        <Route
          path={routePaths.admin}
          element={
            <AdminRoute>
              <PlaceholderPage title="Admin" />
            </AdminRoute>
          }
        />

        {/* Fallback */}
        <Route path="*" element={<PlaceholderPage title="Page not found" />} />
      </Route>
    </Routes>
  )
}