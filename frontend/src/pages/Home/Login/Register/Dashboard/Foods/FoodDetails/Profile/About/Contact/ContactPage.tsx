import { Globe, Mail, MapPin, Phone } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'

interface ContactMethod {
  icon: LucideIcon
  label: string
  value: string
  /** When present, the whole card links out (mailto / tel / URL). */
  href?: string
}

// TODO: replace with the project's real contact details.
const CONTACT_METHODS: ReadonlyArray<ContactMethod> = [
  {
    icon: Mail,
    label: 'Email',
    value: 'hello@nutriguideai.app',
    href: 'mailto:hello@nutriguideai.app',
  },
  {
    icon: Phone,
    label: 'Phone',
    value: '+94 700 000 000',
    href: 'tel:+94700000000',
  },
  {
    icon: MapPin,
    label: 'Location',
    value: 'Colombo, Sri Lanka',
  },
  {
    icon: Globe,
    label: 'Website',
    value: 'nutriguideai.app',
    href: 'https://nutriguideai.app',
  },
]

// TODO: replace with the project's real social profiles.
const SOCIAL_LINKS = [
  { label: 'GitHub', href: 'https://github.com/your-handle' },
  { label: 'LinkedIn', href: 'https://www.linkedin.com/in/your-handle' },
] as const

/**
 * Contact page.
 *
 * Static contact channels — the backend has no messaging endpoint, so
 * cards link out via mailto/tel/URL and social profiles open
 * externally. Socials are plain text links because lucide-react no
 * longer ships brand icons.
 */
export function ContactPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-2xl text-center">
        <span className="inline-flex items-center gap-2 rounded-full border border-border bg-card px-3 py-1 text-xs font-medium text-muted-foreground shadow-sm">
          Get in touch
        </span>
        <h1 className="mt-6 text-4xl font-bold tracking-tight sm:text-5xl">
          We'd love to hear from you
        </h1>
        <p className="mt-5 text-muted-foreground">
          Questions, feedback, or partnership ideas — reach out through any
          channel below.
        </p>
      </div>

      <div className="mx-auto mt-12 grid max-w-4xl gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {CONTACT_METHODS.map((method) => {
          const card = (
            <div className="flex h-full flex-col items-center justify-center rounded-2xl border border-border bg-card p-6 text-center shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-md">
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <method.icon aria-hidden="true" className="h-5 w-5" />
              </span>
              <h2 className="mt-4 font-semibold">{method.label}</h2>
              <p className="mt-1 break-all text-sm text-muted-foreground">
                {method.value}
              </p>
            </div>
          )

          return method.href ? (
            <a
              key={method.label}
              href={method.href}
              className="rounded-2xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              {card}
            </a>
          ) : (
            <div key={method.label}>{card}</div>
          )
        })}
      </div>

      {/* Social links — text-only, no brand icons needed */}
      <div className="mt-14 text-center">
        <h2 className="text-lg font-semibold tracking-tight">Follow us</h2>
        <div className="mt-4 flex items-center justify-center gap-6 text-sm">
          {SOCIAL_LINKS.map((social, index) => (
            <div key={social.label} className="flex items-center gap-6">
              {index > 0 ? (
                <span aria-hidden="true" className="text-muted-foreground/40">
                  ·
                </span>
              ) : null}
              <a
                href={social.href}
                target="_blank"
                rel="noopener noreferrer"
                className="rounded-sm font-medium text-muted-foreground transition-colors hover:text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              >
                {social.label}
              </a>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}