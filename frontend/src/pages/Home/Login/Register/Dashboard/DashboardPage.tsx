import { Link } from 'react-router-dom'

import {
  Activity,
  Flame,
  HeartPulse,
  Pencil,
  Scale,
  Target,
  User as UserIcon,
} from 'lucide-react'
import type { ActivityLevel, Gender, Goal, UserResponse } from '../../../../../types/user'
import { useAuth } from '../../../../../hooks/useAuth'
import { buttonClassName } from '../../../../../components/ui/Button'
import { PageState } from '../../../../../components/ui/PageState'
import { routePaths } from '../../../../../routes/routePaths'
import { Card } from '../../../../../components/ui/Card'
import { formatCalories } from '../../../../../utils/format'

/* ---------- label maps (same values the backend enums use) ---------- */

const GENDER_LABELS: Record<Gender, string> = {
  MALE: 'Male',
  FEMALE: 'Female',
  OTHER: 'Other',
}

const ACTIVITY_LABELS: Record<ActivityLevel, string> = {
  SEDENTARY: 'Sedentary',
  LIGHT: 'Lightly active',
  MODERATE: 'Moderately active',
  ACTIVE: 'Active',
  VERY_ACTIVE: 'Very active',
}

const GOAL_LABELS: Record<Goal, string> = {
  LOSE_WEIGHT: 'Lose weight',
  GAIN_WEIGHT: 'Gain weight',
  MAINTAIN_WEIGHT: 'Maintain weight',
}

/* ---------- health math (same formulas as the Profile page) ---------- */

/** BMI from height (cm) and weight (kg). */
function bmiOf(user: UserResponse): number {
  const heightM = user.height / 100
  return user.weight / (heightM * heightM)
}

/** WHO classification for adult BMI values. */
function bmiCategory(bmi: number): string {
  if (bmi < 18.5) return 'Underweight'
  if (bmi < 25) return 'Normal'
  if (bmi < 30) return 'Overweight'
  return 'Obese'
}

/** BMR via the Mifflin-St Jeor equation. OTHER defaults to the male formula. */
function bmrOf(user: UserResponse): number {
  const base = 10 * user.weight + 6.25 * user.height - 5 * user.age
  return user.gender === 'FEMALE' ? base - 161 : base + 5
}

const ACTIVITY_MULTIPLIERS: Record<ActivityLevel, number> = {
  SEDENTARY: 1.2,
  LIGHT: 1.375,
  MODERATE: 1.55,
  ACTIVE: 1.725,
  VERY_ACTIVE: 1.9,
}

const GOAL_ADJUSTMENT: Record<Goal, number> = {
  LOSE_WEIGHT: -500,
  GAIN_WEIGHT: 500,
  MAINTAIN_WEIGHT: 0,
}

/** Daily calorie target = BMR × activity multiplier + goal adjustment. */
function dailyCaloriesOf(user: UserResponse): number {
  const bmr = bmrOf(user)
  const multiplier = ACTIVITY_MULTIPLIERS[user.activityLevel]
  const adjustment = GOAL_ADJUSTMENT[user.goal]
  return Math.round(bmr * multiplier + adjustment)
}

/* ---------- loading skeleton ---------- */

function DashboardSkeleton() {
  return (
    <div className="animate-pulse space-y-6">
      <div className="h-8 w-64 rounded-lg bg-muted" />
      <div className="h-4 w-80 max-w-full rounded bg-muted" />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {[0, 1, 2, 3].map((i) => (
          <div key={i} className="h-36 rounded-xl border border-border bg-card" />
        ))}
      </div>
    </div>
  )
}

/* ---------- page ---------- */

/** Signed-in dashboard — daily nutrition snapshot from the user profile. */
export function DashboardPage() {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <DashboardSkeleton />
  }

  if (!user) {
    return (
      <PageState
        icon={UserIcon}
        title="No profile data"
        message="Sign in and complete your health profile to see your personalized daily targets."
        action={
          <Link
            to={routePaths.login}
            className={buttonClassName('primary', 'md')}
          >
            Sign in
          </Link>
        }
      />
    )
  }

  const bmi = bmiOf(user)
  const bmr = bmrOf(user)
  const dailyCalories = dailyCaloriesOf(user)

  const stats = [
    {
      icon: Scale,
      label: 'BMI',
      value: bmi.toFixed(1),
      caption: bmiCategory(bmi),
    },
    {
      icon: Flame,
      label: 'BMR',
      value: formatCalories(bmr),
      caption: 'Resting burn',
    },
    {
      icon: Target,
      label: 'Daily target',
      value: formatCalories(dailyCalories),
      caption: GOAL_LABELS[user.goal],
    },
    {
      icon: Activity,
      label: 'Activity',
      value: ACTIVITY_LABELS[user.activityLevel].split(' ')[0],
      caption: 'Your activity level',
    },
  ] as const

  const profileRows = [
    { label: 'Age', value: String(user.age) },
    { label: 'Gender', value: GENDER_LABELS[user.gender] },
    { label: 'Height', value: `${user.height} cm` },
    { label: 'Weight', value: `${user.weight} kg` },
  ] as const

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
      {/* Header */}
      <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Welcome back, {user.firstName}
          </h1>
          <p className="mt-1 text-muted-foreground">
            Here's your personalized nutrition snapshot for today.
          </p>
        </div>
        <Link
          to={routePaths.profile}
          className={buttonClassName('outline', 'md')}
        >
          <Pencil aria-hidden="true" className="h-4 w-4" />
          Update profile
        </Link>
      </header>

      {/* Stat cards */}
      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <Card key={stat.label} className="flex flex-col justify-between">
            <div className="flex items-center justify-between">
              <p className="text-sm font-medium text-muted-foreground">
                {stat.label}
              </p>
              <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <stat.icon aria-hidden="true" className="h-4 w-4" />
              </span>
            </div>
            <p className="mt-4 text-2xl font-bold tracking-tight">{stat.value}</p>
            <p className="mt-1 text-xs text-muted-foreground">{stat.caption}</p>
          </Card>
        ))}
      </div>

      {/* Profile snapshot + how calories are calculated */}
      <div className="mt-8 grid gap-4 lg:grid-cols-2">
        <Card
          title="Profile snapshot"
          description="The health data driving your plan"
          action={
            <Link
              to={routePaths.profile}
              className="text-sm font-medium text-primary transition-colors hover:text-primary/80"
            >
              Edit
            </Link>
          }
        >
          <dl className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            {profileRows.map((row) => (
              <div key={row.label}>
                <dt className="text-xs text-muted-foreground">{row.label}</dt>
                <dd className="mt-1 text-sm font-semibold text-foreground">
                  {row.value}
                </dd>
              </div>
            ))}
          </dl>
        </Card>

        <Card title="How your target is calculated">
          <ul className="space-y-3 text-sm text-muted-foreground">
            <li className="flex gap-2.5">
              <HeartPulse
                aria-hidden="true"
                className="mt-0.5 h-4 w-4 shrink-0 text-primary"
              />
              <span>
                <strong className="font-medium text-foreground">BMR</strong> — the
                calories your body burns at rest, from the Mifflin-St Jeor
                equation.
              </span>
            </li>
            <li className="flex gap-2.5">
              <Activity
                aria-hidden="true"
                className="mt-0.5 h-4 w-4 shrink-0 text-primary"
              />
              <span>
                <strong className="font-medium text-foreground">Activity</strong> — your
                level multiplies BMR by{' '}
                {ACTIVITY_MULTIPLIERS[user.activityLevel]}×.
              </span>
            </li>
            <li className="flex gap-2.5">
              <Target
                aria-hidden="true"
                className="mt-0.5 h-4 w-4 shrink-0 text-primary"
              />
              <span>
                <strong className="font-medium text-foreground">Goal</strong> —{' '}
                {GOAL_ADJUSTMENT[user.goal] >= 0 ? 'adds' : 'subtracts'}{' '}
                {Math.abs(GOAL_ADJUSTMENT[user.goal])} kcal/day for{' '}
                {GOAL_LABELS[user.goal].toLowerCase()}.
              </span>
            </li>
          </ul>
          <div className="mt-6">
            <Link
              to={routePaths.profile}
              className={buttonClassName('outline', 'md', 'w-full')}
            >
              <Pencil aria-hidden="true" className="h-4 w-4" />
              Update your health profile
            </Link>
          </div>
        </Card>
      </div>
    </div>
  )
}