import AppRoutes from './routes/AppRoutes'

/**
 * Root component of NutriGuide AI.
 *
 * Provides the base application layout and global semantic styling.
 * The routing layer will be rendered inside this component.
 */
export default function App() {
  return (
    <main className="min-h-screen bg-background text-foreground antialiased">
      <AppRoutes />
    </main>
  );
}

