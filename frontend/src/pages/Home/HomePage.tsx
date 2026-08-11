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
import { buttonClassName } from '../../components/ui/Button'

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

  const primaryCta = isAuthenticated ? routePaths.dashboard : routePaths.register
  const heroCtaLabel = isAuthenticated
    ? 'Go to dashboard'
    : 'Get started — it’s free'

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden">
        {/* Decorative ambient glow — theme-token driven */}
        <div
          aria-hidden="true"
          className="pointer-events-none absolute -top-32 left-1/2 h-96 w-96 -translate-x-1/2 rounded-full bg-primary/10 blur-3xl"
        />

        <div className="relative mx-auto max-w-7xl px-4 pb-20 pt-20 text-center sm:px-6 sm:pb-28 sm:pt-28 lg:px-8">
          <span className="inline-flex items-center gap-2 rounded-full border border-border bg-card px-3 py-1 text-xs font-medium text-muted-foreground shadow-sm">
            <span
              aria-hidden="true"
              className="h-1.5 w-1.5 rounded-full bg-primary"
            />
            Evidence-informed · Personalized · Practical
          </span>

          <h1 className="mx-auto mt-6 max-w-3xl text-4xl font-bold leading-tight tracking-tight sm:text-5xl lg:text-6xl">
            Your personal nutrition plan,{' '}
            <span className="text-primary">built for you</span>
          </h1>

          <p className="mx-auto mt-5 max-w-xl text-base leading-relaxed text-muted-foreground sm:text-lg">
            NutriGuide AI turns your health profile, goals, lifestyle, and
            budget into meal recommendations you can actually follow.
          </p>

          <div className="mt-9 flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link
              to={primaryCta}
              className={buttonClassName('primary', 'lg', 'w-full sm:w-auto')}
            >
              {heroCtaLabel}
            </Link>
            <Link
              to={routePaths.foods}
              className={buttonClassName('outline', 'lg', 'w-full sm:w-auto')}
            >
              Explore foods
            </Link>
          </div>

          <p className="mt-6 text-xs text-muted-foreground">
            Free forever · No credit card required
          </p>
        </div>
      </section>

      {/* Features */}
      <section
        aria-labelledby="features-heading"
        className="mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8"
      >
        <div className="mx-auto max-w-2xl text-center">
          <h2
            id="features-heading"
            className="text-3xl font-bold tracking-tight sm:text-4xl"
          >
            Everything you need to eat better
          </h2>
          <p className="mt-3 text-muted-foreground">
            One platform that understands your body, your goals, and your
            budget.
          </p>
        </div>

        <div className="mt-14 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map((feature) => (
            <article
              key={feature.title}
              className="group rounded-2xl border border-border bg-card p-6 shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-md"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary transition-colors group-hover:bg-primary/15">
                <feature.icon aria-hidden="true" className="h-5 w-5" />
              </span>
              <h3 className="mt-4 font-semibold text-foreground">
                {feature.title}
              </h3>
              <p className="mt-1.5 text-sm leading-relaxed text-muted-foreground">
                {feature.description}
              </p>
            </article>
          ))}
        </div>
      </section>

      {/* Stats band */}
      <section
        aria-label="NutriGuide AI in numbers"
        className="border-y border-border bg-muted/50"
      >
        <div className="mx-auto grid max-w-7xl grid-cols-1 gap-10 px-4 py-16 sm:grid-cols-3 sm:px-6 lg:px-8">
          {STATS.map((stat) => (
            <div key={stat.label} className="text-center">
              <p className="text-4xl font-bold tracking-tight text-primary">
                {stat.value}
              </p>
              <p className="mt-1.5 text-sm text-muted-foreground">
                {stat.label}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Closing CTA */}
      <section
        aria-labelledby="cta-heading"
        className="mx-auto max-w-7xl px-4 py-24 text-center sm:px-6 lg:px-8"
      >
        <h2
          id="cta-heading"
          className="mx-auto max-w-2xl text-3xl font-bold tracking-tight sm:text-4xl"
        >
          Stop guessing. Start eating for your goals.
        </h2>
        <p className="mx-auto mt-4 max-w-xl text-muted-foreground">
          Build your health profile in minutes and get your first personalized
          meal plan today.
        </p>
        <Link
          to={primaryCta}
          className={buttonClassName('primary', 'lg', 'mt-9')}
        >
          {isAuthenticated ? 'Open your dashboard' : 'Create your free account'}
        </Link>
      </section>
    </div>
  )
}