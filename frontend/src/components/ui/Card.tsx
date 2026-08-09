import type { ReactNode } from 'react'

interface CardProps {
  /** Optional header title. */
  title?: string
  /** Optional muted description under the title. */
  description?: string
  /** Optional right-aligned header action (link or button). */
  action?: ReactNode
  children: ReactNode
  className?: string
}

/** Shared surface for grouping related content with an optional header. */
export function Card({
  title,
  description,
  action,
  children,
  className = '',
}: CardProps) {
  return (
    <div className={`rounded-xl border border-border bg-card ${className}`.trim()}>
      {title || action ? (
        <div className="flex items-start justify-between gap-4 border-b border-border px-5 py-4">
          <div className="min-w-0">
            {title ? (
              <h2 className="text-sm font-semibold text-foreground">{title}</h2>
            ) : null}
            {description ? (
              <p className="mt-0.5 text-xs text-muted-foreground">
                {description}
              </p>
            ) : null}
          </div>
          {action}
        </div>
      ) : null}
      <div className="p-5">{children}</div>
    </div>
  )
}