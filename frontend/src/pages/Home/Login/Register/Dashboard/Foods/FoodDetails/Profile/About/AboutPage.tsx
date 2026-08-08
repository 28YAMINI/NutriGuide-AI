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

/** Public "About" page — mission, how it works, values and a CTA. */
export function AboutPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
      {/* Hero */}
      <header className="mx-auto max-w-2xl text-center">
        <span className="inline-flex items-center rounded-full border border-border bg-card px-3 py-1 text-xs font-medium text-muted-foreground">
          About NutriGuide AI
        </span>
        <h1 className="mt-4 text-3xl font-bold tracking-tight sm:text-4xl">
          Nutrition guidance that fits your life
        </h1>
        <p className="mt-4 text-muted-foreground">
          NutriGuide AI turns your health profile — age, body metrics,
          activity, goals and food preferences — into practical, personalized
          meal recommendations. No generic influencer diets, just a plan built
          around you.
        </p>
      </header>

      {/* Stats band */}
      <div className="mt-12 grid grid-cols-2 gap-4 lg:grid-cols-4">
        {STATS.map((stat) => (
          <div
            key={stat.label}
            className="rounded-xl border border-border bg-card p-5 text-center"
          >
            <p className="text-2xl font-bold text-primary">{stat.value}</p>
            <p className="mt-1 text-sm text-muted-foreground">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* How it works */}
      <section className="mt-16">
        <h2 className="text-center text-2xl font-bold tracking-tight">
          How it works
        </h2>
        <div className="mt-8 grid gap-4 md:grid-cols-3">
          {STEPS.map((step, index) => (
            <div
              key={step.title}
              className="relative rounded-xl border border-border bg-card p-6"
            >
              <span className="absolute right-4 top-4 text-xs font-semibold text-muted-foreground/60">
                0{index + 1}
              </span>
              <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
                <step.icon aria-hidden="true" className="h-5 w-5" />
              </span>
              <h3 className="mt-4 font-semibold">{step.title}</h3>
              <p className="mt-1.5 text-sm text-muted-foreground">
                {step.description}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* Values */}
      <section className="mt-16">
        <h2 className="text-center text-2xl font-bold tracking-tight">
          What we care about
        </h2>
        <div className="mt-8 grid gap-4 md:grid-cols-3">
          {VALUES.map((value) => (
            <div
              key={value.title}
              className="rounded-xl border border-border bg-card p-6"
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
      </section>

      {/* CTA */}
      <section className="mt-16 rounded-2xl border border-border bg-card p-8 text-center sm:p-12">
        <h2 className="text-2xl font-bold tracking-tight">
          Ready to eat smarter?
        </h2>
        <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
          Create your free account and get a plan personalized to your goals in
          minutes.
        </p>
        <Link
          to={routePaths.register}
          className="mt-6 inline-flex items-center gap-2 rounded-lg bg-primary px-5 py-2.5 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90"
        >
          Get started
          <ArrowRight aria-hidden="true" className="h-4 w-4" />
        </Link>
      </section>
    </div>
  )
}