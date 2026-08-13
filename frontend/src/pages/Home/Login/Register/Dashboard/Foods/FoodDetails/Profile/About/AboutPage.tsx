import { Link } from 'react-router-dom'

import {
  ArrowRight,
  BookOpen,
  Scale,
  ShieldCheck,
  Sparkles,
  TrendingUp,
  User,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { routePaths } from '../../../../../../../../../routes/routePaths'
import { buttonClassName } from '../../../../../../../../../components/ui/Button'


interface Stat {
  value: string
  label: string
}

interface Step {
  icon: LucideIcon
  title: string
  description: string
}

interface Value {
  icon: LucideIcon
  title: string
  description: string
}

const STATS: ReadonlyArray<Stat> = [
  { value: '10', label: 'Food categories' },
  { value: '3', label: 'Health goals' },
  { value: '5', label: 'Activity levels' },
  { value: '100%', label: 'Personalized plans' },
]

const STEPS: ReadonlyArray<Step> = [
  {
    icon: User,
    title: 'Build your profile',
    description:
      'Share your age, body metrics, activity level and health goals in under two minutes.',
  },
  {
    icon: Sparkles,
    title: 'Get a personalized plan',
    description:
      'Your daily calorie and nutrition targets are calculated from your unique data — not generic advice.',
  },
  {
    icon: TrendingUp,
    title: 'Track and refine',
    description:
      'Update your profile as you progress and keep your plan aligned with your goals.',
  },
]

const VALUES: ReadonlyArray<Value> = [
  {
    icon: BookOpen,
    title: 'Evidence-informed',
    description:
      'Recommendations follow established nutrition guidelines and your health profile.',
  },
  {
    icon: Scale,
    title: 'Practical & budget-aware',
    description:
      'Plans respect your lifestyle and budget with locally available foods.',
  },
  {
    icon: ShieldCheck,
    title: 'Privacy-first',
    description:
      'Your health data is yours. It is only used to personalize your experience.',
  },
]

/**
 * About page.
 *
 * Hero → stats → how it works → values → CTA. Ends with a register
 * action so curiosity converts into an account.
 */
export function AboutPage() {
  return (
    <div>
      {/* Hero */}
      <section className="mx-auto max-w-7xl px-4 py-16 text-center sm:px-6 sm:py-20 lg:px-8">
        <span className="inline-flex items-center gap-2 rounded-full border border-border bg-card px-3 py-1 text-xs font-medium text-muted-foreground shadow-sm">
          About NutriGuide AI
        </span>
        <h1 className="mx-auto mt-6 max-w-2xl text-4xl font-bold tracking-tight sm:text-5xl">
          Nutrition guidance that{' '}
          <span className="text-primary">starts with you</span>
        </h1>
        <p className="mx-auto mt-5 max-w-xl text-muted-foreground">
          NutriGuide AI turns your personal health profile into practical,
          everyday food decisions — built on evidence, not trends.
        </p>
      </section>

      {/* Stats */}
      <section
        aria-label="NutriGuide AI in numbers"
        className="border-y border-border bg-muted/50"
      >
        <div className="mx-auto grid max-w-7xl grid-cols-2 gap-8 px-4 py-12 sm:px-6 lg:grid-cols-4 lg:px-8">
          {STATS.map((stat) => (
            <div key={stat.label} className="text-center">
              <p className="text-3xl font-bold tracking-tight text-primary">
                {stat.value}
              </p>
              <p className="mt-1 text-sm text-muted-foreground">{stat.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* How it works */}
      <section
        aria-labelledby="steps-heading"
        className="mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8"
      >
        <div className="mx-auto max-w-2xl text-center">
          <h2
            id="steps-heading"
            className="text-3xl font-bold tracking-tight sm:text-4xl"
          >
            How it works
          </h2>
          <p className="mt-3 text-muted-foreground">
            Three steps from profile to plan.
          </p>
        </div>

        <ol className="mt-12 grid gap-5 sm:grid-cols-3">
          {STEPS.map((step, index) => (
            <li
              key={step.title}
              className="relative rounded-2xl border border-border bg-card p-6 shadow-sm"
            >
              <span
                aria-hidden="true"
                className="absolute right-4 top-4 text-3xl font-bold text-muted-foreground/15"
              >
                {index + 1}
              </span>
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <step.icon aria-hidden="true" className="h-5 w-5" />
              </span>
              <h3 className="mt-4 font-semibold">{step.title}</h3>
              <p className="mt-1.5 text-sm text-muted-foreground">
                {step.description}
              </p>
            </li>
          ))}
        </ol>
      </section>

      {/* Values */}
      <section
        aria-labelledby="values-heading"
        className="border-t border-border bg-muted/30"
      >
        <div className="mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-2xl text-center">
            <h2
              id="values-heading"
              className="text-3xl font-bold tracking-tight sm:text-4xl"
            >
              What we stand for
            </h2>
            <p className="mt-3 text-muted-foreground">
              The principles behind every recommendation.
            </p>
          </div>

          <div className="mt-12 grid gap-5 sm:grid-cols-3">
            {VALUES.map((value) => (
              <div
                key={value.title}
                className="rounded-2xl border border-border bg-card p-6 shadow-sm"
              >
                <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                  <value.icon aria-hidden="true" className="h-5 w-5" />
                </span>
                <h3 className="mt-4 font-semibold">{value.title}</h3>
                <p className="mt-1.5 text-sm text-muted-foreground">
                  {value.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section
        aria-labelledby="cta-heading"
        className="mx-auto max-w-7xl px-4 py-20 text-center sm:px-6 lg:px-8"
      >
        <h2
          id="cta-heading"
          className="mx-auto max-w-2xl text-3xl font-bold tracking-tight sm:text-4xl"
        >
          Ready to eat for your goals?
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted-foreground">
          Build your profile in minutes and get a plan that fits your life.
        </p>
        <Link
          to={routePaths.register}
          className={buttonClassName('primary', 'lg', 'mt-8')}
        >
          Get started
          <ArrowRight aria-hidden="true" className="h-4 w-4" />
        </Link>
      </section>
    </div>
  )
}