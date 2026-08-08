import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

import { Activity, Scale, ShieldCheck, Target, Utensils } from 'lucide-react'
import type { ActivityLevel, Goal } from '../../../../../types/user'
import { useAuth } from '../../../../../hooks/useAuth'
import { routePaths } from '../../../../../routes/routePaths'



const GOAL_LABELS: Record<Goal, string> = {
  LOSE_WEIGHT: 'Lose weight',
  GAIN_WEIGHT: 'Gain weight',
  MAINTAIN_WEIGHT: 'Maintain weight',
}

const ACTIVITY_LABELS: Record<ActivityLevel, string> = {
  SEDENTARY: 'Sedentary (little or no exercise)',
  LIGHT: 'Light (1–2 days/week)',
  MODERATE: 'Moderate (3–5 days/week)',
  ACTIVE: 'Active (6–7 days/week)',
  VERY_ACTIVE: 'Very active (intense daily exercise)',
}

interface BmiCategory {
  label: string
  tone: string
}

/** WHO classification for adult BMI values. */
function bmiCategory(bmi: number): BmiCategory {
  if (bmi < 18.5) {
    return { label: 'Underweight', tone: 'text-amber-600 dark:text-amber-400' }
  }
  if (bmi < 25) {
    return {
      label: 'Healthy range',
      tone: 'text-emerald-600 dark:text-emerald-400',
    }
  }
  if (bmi < 30) {
    return { label: 'Overweight', tone: 'text-amber-600 dark:text-amber-400' }
  }
  return { label: 'Obese', tone: 'text-red-600 dark:text-red-400' }
}

interface SummaryCardProps {
  icon: ReactNode
  label: string
  children: ReactNode
}

/** Reusable stat tile used for the BMI, goal, and activity summaries. */
function SummaryCard({ icon, label, children }: SummaryCardProps) {
  return (
    <div className="rounded-xl border border-border bg-background p-6">
      <div className="flex items-center gap-2 text-muted-foreground">
        {icon}
        <h2 className="text-sm font-medium">{label}</h2>
      </div>
      <div className="mt-4">{children}</div>
    </div>
  )
}

/**
 * Signed-in home page.
 *
 * Renders a profile snapshot from the session user (AuthContext) and
 * quick links into the food catalog and admin panel. BMI is derived
 * client-side from height (cm) and weight (kg).
 */
export function DashboardPage() {
  const { user, isAdmin } = useAuth()

  // ProtectedRoute guarantees a session; this guards the type only.
  if (!user) {
    return null
  }

  const heightInM = (user.height ?? 0) / 100
  const weightKg = user.weight ?? 0
  const bmi =
    heightInM > 0 && weightKg > 0
      ? Math.round((weightKg / (heightInM * heightInM)) * 10) / 10
      : null
  const category = bmi !== null ? bmiCategory(bmi) : null

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      <header>
        <h1 className="text-3xl font-semibold tracking-tight">
          Welcome back, {user.firstName}
        </h1>
        <p className="mt-1.5 text-sm text-muted-foreground">
          A snapshot of your nutrition profile.
        </p>
      </header>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <SummaryCard icon={<Scale className="h-4 w-4" />} label="Body Mass Index">
          {bmi !== null && category ? (
            <>
              <p className="text-3xl font-semibold tracking-tight">{bmi}</p>
              <p className={`mt-1 text-sm font-medium ${category.tone}`}>
                {category.label}
              </p>
            </>
          ) : (
            <>
              <p className="text-3xl font-semibold tracking-tight text-muted-foreground">
                —
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                Add your height and weight to see your BMI.
              </p>
            </>
          )}
        </SummaryCard>

        <SummaryCard icon={<Target className="h-4 w-4" />} label="Health Goal">
          <p className="text-lg font-medium">
            {user.goal ? GOAL_LABELS[user.goal] : 'Not set yet'}
          </p>
        </SummaryCard>

        <SummaryCard
          icon={<Activity className="h-4 w-4" />}
          label="Activity Level"
        >
          <p className="text-lg font-medium">
            {user.activityLevel
              ? ACTIVITY_LABELS[user.activityLevel]
              : 'Not set yet'}
          </p>
        </SummaryCard>
      </div>

      <section className="mt-12" aria-labelledby="quick-actions-heading">
        <h2
          id="quick-actions-heading"
          className="text-lg font-semibold tracking-tight"
        >
          Quick actions
        </h2>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <Link
            to={routePaths.foods}
            className="group rounded-xl border border-border bg-background p-6 transition-colors hover:border-primary/40 hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          >
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
              <Utensils className="h-5 w-5" />
            </span>
            <span className="mt-4 block text-sm font-semibold">Browse foods</span>
            <span className="mt-1 block text-sm text-muted-foreground">
              Search the catalog, filter by category, and view nutrition details.
            </span>
          </Link>

          {isAdmin && (
            <Link
              to={routePaths.admin}
              className="group rounded-xl border border-border bg-background p-6 transition-colors hover:border-primary/40 hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                <ShieldCheck className="h-5 w-5" />
              </span>
              <span className="mt-4 block text-sm font-semibold">Admin panel</span>
              <span className="mt-1 block text-sm text-muted-foreground">
                Add, edit, and remove food items from the catalog.
              </span>
            </Link>
          )}
        </div>
      </section>
    </div>
  )
}