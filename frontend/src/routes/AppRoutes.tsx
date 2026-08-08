import { Route, Routes } from 'react-router-dom'



import { AdminRoute, ProtectedRoute, PublicRoute } from './guards'
import { routePaths } from './routePaths'
import { MainLayout } from '../components/layout/MainLayout'
import { HomePage } from '../pages/Home/HomePage'
import { FoodsPage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodPages'
import { FoodDetailsPage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/FoodDetailsPage'
import { AboutPage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/Profile/About/AboutPage'
import { LoginPage } from '../pages/Home/Login/LoginPage'
import { DashboardPage } from '../pages/Home/Login/Register/Dashboard/DashboardPage'
import { RegisterPage } from '../pages/Home/Login/Register/RegisterPage'
import { NotFoundPage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/Profile/About/Contact/Admin/NotFound/NotFoundPage'
import { AdminPage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/Profile/About/Contact/Admin/AdminPage'
import { ContactPage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/Profile/About/Contact/ContactPage'
import { ProfilePage } from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/Profile/ProfilePage'

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
        <Route path={routePaths.home} element={<HomePage />} />
        <Route path={routePaths.foods} element={<FoodsPage />} />
        <Route path={routePaths.foodDetails} element={<FoodDetailsPage />} />
        <Route path={routePaths.about} element={<AboutPage />} />
        <Route path={routePaths.contact} element={<ContactPage />} />

        {/* Auth (signed-out only) */}
        <Route
          path={routePaths.login}
          element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          }
        />
        <Route
          path={routePaths.register}
          element={
            <PublicRoute>
              <RegisterPage />
            </PublicRoute>
          }
        />

        {/* Protected (signed-in only) */}
        <Route
          path={routePaths.dashboard}
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path={routePaths.profile}
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />

        {/* Admin (ADMIN role only) */}
        <Route
          path={routePaths.admin}
          element={
            <AdminRoute>
              <AdminPage />
            </AdminRoute>
          }
        />

        {/* Fallback — must stay last */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}