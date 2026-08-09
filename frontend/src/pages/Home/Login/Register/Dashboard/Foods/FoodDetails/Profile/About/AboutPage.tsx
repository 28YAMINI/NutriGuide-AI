import { Link } from 'react-router-dom'

import type { LucideIcon } from 'lucide-react'
import {
  ArrowRight,
  BookOpen,
  Scale,
  ShieldCheck,
  Sparkles,
  TrendingUp,
  User,
} from 'lucide-react'
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

/** Public about page — what NutriGuide AI is and how it works. */
export function AboutPage() {
  return (
    <main className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      {/* Hero */}
      <header className="mx-auto max-w-2xl text-center">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
          Nutrition advice that actually fits you
        </h1>
        <p className="mt-4 text-muted-foreground">
          NutriGuide AI turns your age, body metrics, activity level and goals
          into a personalized daily nutrition plan — no generic diet advice.
        </p>
      </header>

      {/* Stats */}
      <section
        className="mt-12 grid grid-cols-2 gap-4 lg:grid-cols-4"
        aria-label="NutriGuide AI in numbers"
      >
        {STATS.map((stat) => (
          <div
            key={stat.label}
            className="rounded-xl border border-border bg-card p-6 text-center"
          >
            <p className="text-3xl font-bold tracking-tight text-primary">
              {stat.value}
            </p>
            <p className="mt-1 text-sm text-muted-foreground">{stat.label}</p>
          </div>
        ))}
      </section>

      {/* How it works */}
      <section className="mt-16">
        <h2 className="text-2xl font-bold tracking-tight">How it works</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-3">
          {STEPS.map((step) => (
            <div
              key={step.title}
              className="rounded-xl border border-border bg-card p-6"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <step.icon aria-hidden="true" className="h-5 w-5" />
              </span>
              <h3 className="mt-4 text-sm font-semibold text-foreground">
                {step.title}
              </h3>
              <p className="mt-2 text-sm text-muted-foreground">
                {step.description}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Values */}
      <section className="mt-16">
        <h2 className="text-2xl font-bold tracking-tight">What we value</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-3">
          {VALUES.map((value) => (
            <div
              key={value.title}
              className="rounded-xl border border-border bg-card p-6"
            >
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <value.icon aria-hidden="true" className="h-5 w-5" />
              </span>
              <h3 className="mt-4 text-sm font-semibold text-foreground">
                {value.title}
              </h3>
              <p className="mt-2 text-sm text-muted-foreground">
                {value.description}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="mt-16 rounded-xl border border-border bg-card px-6 py-12 text-center">
        <h2 className="text-2xl font-bold tracking-tight">Ready to start?</h2>
        <p className="mx-auto mt-2 max-w-md text-muted-foreground">
          Create your free profile and get a plan built around your goals.
        </p>
        <div className="mt-6">
          <Link to={routePaths.register} className={buttonClassName('primary', 'lg')}>
            Get started
            <ArrowRight aria-hidden="true" className="h-4 w-4" />
          </Link>
        </div>
      </section>
    </main>
  )
}