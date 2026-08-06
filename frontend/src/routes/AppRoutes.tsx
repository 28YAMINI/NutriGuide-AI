import { Route, Routes } from 'react-router-dom'

import { routePaths } from './routePaths'
import NotFoundPage from '../pages/Home/Login/Register/Dashboard/Foods/FoodDetails/Profile/About/Contact/Admin/NotFound/NotFoundPage'


/**
 * Defines the application's route configuration.
 *
 * Public, protected, and admin routes are registered here.
 */
function PlaceholderPage({ title }: { title: string }) {
  return (
    <h1 className="py-16 text-center text-2xl font-semibold text-foreground">
      {title}
    </h1>
  )
}

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path={routePaths.home} element={<PlaceholderPage title="Home Page" />} />
      <Route path={routePaths.login} element={<PlaceholderPage title="Login Page" />} />
      <Route path={routePaths.register} element={<PlaceholderPage title="Register Page" />} />
      <Route path={routePaths.about} element={<PlaceholderPage title="About Page" />} />
      <Route path={routePaths.contact} element={<PlaceholderPage title="Contact Page" />} />

      {/* Protected */}
      <Route path={routePaths.dashboard} element={<PlaceholderPage title="Dashboard Page" />} />
      <Route path={routePaths.foods} element={<PlaceholderPage title="Foods Page" />} />
      <Route path={routePaths.foodDetails} element={<PlaceholderPage title="Food Details Page" />} />
      <Route path={routePaths.profile} element={<PlaceholderPage title="Profile Page" />} />

      {/* Admin */}
      <Route path={routePaths.admin} element={<PlaceholderPage title="Admin Page" />} />

      {/* Fallback */}
      <Route path={routePaths.notFound} element={<NotFoundPage />} />
    </Routes>
  )
}