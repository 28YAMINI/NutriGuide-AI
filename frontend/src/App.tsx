import AppRoutes from './routes/AppRoutes'

/**
 * Root component of NutriGuide AI.
 *
 * Provides the base application shell and global styling. MainLayout
 * (rendered by AppRoutes) owns the <main> landmark.
 */
export default function App() {
  return (
    <div className="min-h-screen bg-background text-foreground antialiased">
      <AppRoutes />
    </div>
  )
}