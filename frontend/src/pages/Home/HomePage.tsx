import { Link } from 'react-router-dom'

import {
  Activity,
  Calendar,
  Heart,
  Sparkles,
  Utensils,
  Wallet,
} from 'lucide-react'

import { useAuth } from '../../hooks/useAuth'
import { routePaths } from '../../routes/routePaths'

const FEATURES = [
  {
    icon: Calendar,
    title: 'Personalized meal plans',
    description:
      'Daily and weekly plans built around your goals, activity level, and food preferences.',
  },
  {
    icon: Utensils,
    title: 'Smart food diary',
    description:
      'Log your meals and track calories and nutrients without the guesswork.',
  },
  {
    icon: Wallet,
    title: 'Budget-friendly choices',
    description:
      'Recommendations that respect your budget and use locally available ingredients.',
  },
  {
    icon: Activity,
    title: 'Progress tracking',
    description:
      'Watch your weight, BMI, and nutrition trends improve over time.',
  },
  {
    icon: Heart,
    title: 'Health-condition aware',
    description:
      'Plans that account for conditions like diabetes, hypertension, and more.',
  },
  {
    icon: Sparkles,
    title: 'AI-powered guidance',
    description:
      'Evidence-informed recommendations tailored to your unique health profile.',
  },
] as const

const STATS = [
  { value: '10k+', label: 'Meals recommended' },
  { value: '6', label: 'Food categories' },
  { value: '100%', label: 'Personalized plans' },
] as const

/**
 * Landing page.
 *
 * Hero → features → stats → closing CTA. All CTAs are auth-aware:
 * signed-out visitors are sent to register/login, signed-in visitors
 * straight to the dashboard.
 */
export function HomePage() {
  const { isAuthenticated } = useAuth()

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden">
        {/* Decorative blurred blob — theme-token driven */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -top-24 left-1/2 h-72 w-72 -translate-x-1/2 rounded-full bg-primary/10 blur-3xl"
        />
        <div className="relative mx-auto max-w-7xl px-4 py-24 text-center sm:px-6 sm:py-32 lg:px-8">
          <span className="inline-flex items-center rounded-full border border-border bg-muted px-3 py-1 text-xs font-medium text-muted-foreground">
            Evidence-informed · Personalized · Practical
          </span>
          <h1 className="mx-auto mt-6 max-w-3xl text-4xl font-semibold tracking-tight sm:text-5xl">
            Your personal nutrition plan,{' '}
            <span className="text-primary">built for you</span>
          </h1>
          <p className="mx-auto mt-5 max-w-xl text-base text-muted-foreground">
            NutriGuide AI turns your health profile, goals, lifestyle, and
            budget into meal recommendations you can actually follow.
          </p>
          <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link
              to={isAuthenticated ? routePaths.dashboard : routePaths.register}
              className="w-full rounded-lg bg-primary px-5 py-2.5 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 sm:w-auto"
            >
              {isAuthenticated ? 'Go to dashboard' : 'Get started — it’s free'}
            </Link>
            <Link
              to={routePaths.foods}
              className="w-full rounded-lg border border-border bg-background px-5 py-2.5 text-sm font-semibold text-foreground transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary sm:w-auto"
            >
              Explore foods
            </Link>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-3xl font-semibold tracking-tight">
            Everything you need to eat better
          </h2>
          <p className="mt-3 text-muted-foreground">
            One platform that understands your body, your goals, and your
            budget.
          </p>
        </div>
        <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map((feature) => (
            <div
              key={feature.title}
              className="rounded-2xl border border-border bg-card p-6 transition-shadow hover:shadow-md"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <feature.icon className="h-5 w-5" />
              </span>
              <h3 className="mt-4 font-semibold">{feature.title}</h3>
              <p className="mt-1.5 text-sm text-muted-foreground">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Stats band */}
      <section className="border-y border-border bg-muted/50">
        <div className="mx-auto grid max-w-7xl grid-cols-1 gap-8 px-4 py-14 sm:grid-cols-3 sm:px-6 lg:px-8">
          {STATS.map((stat) => (
            <div key={stat.label} className="text-center">
              <p className="text-3xl font-semibold tracking-tight text-primary">
                {stat.value}
              </p>
              <p className="mt-1 text-sm text-muted-foreground">{stat.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Closing CTA */}
      <section className="mx-auto max-w-7xl px-4 py-24 text-center sm:px-6 lg:px-8">
        <h2 className="mx-auto max-w-2xl text-3xl font-semibold tracking-tight">
          Stop guessing. Start eating for your goals.
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
          Build your health profile in minutes and get your first
          personalized meal plan today.
        </p>
        <Link
          to={isAuthenticated ? routePaths.dashboard : routePaths.register}
          className="mt-8 inline-flex rounded-lg bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        >
          {isAuthenticated ? 'Open your dashboard' : 'Create your free account'}
        </Link>
      </section>
    </div>
  )
}