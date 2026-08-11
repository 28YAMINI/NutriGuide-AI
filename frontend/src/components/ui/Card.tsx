import type { ReactNode } from 'react'

interface CardProps {
  /** Optional heading rendered in the card header row. */
  title?: ReactNode
  /** Optional muted description below the title. */
  description?: ReactNode
  /** Optional element on the right of the header row (link, button). */
  action?: ReactNode
  /** Removes default body padding for custom layouts. */
  noPadding?: boolean
  className?: string
  children: ReactNode
}

/**
 * Shared card primitive.
 *
 * Standardizes the surface every section uses: subtle border, soft
 * shadow, rounded corners, and an optional header (title / description
 * / action). Consumers pass className only to tweak layout.
 */
export function Card({
  title,
  description,
  action,
  noPadding = false,
  className = '',
  children,
}: CardProps) {
  const hasHeader = Boolean(title || description || action)

  return (
    <div
      className={`rounded-xl border border-border bg-card text-card-foreground shadow-sm ${className}`.trim()}
    >
      {hasHeader ? (
        <header className="flex items-start justify-between gap-4 border-b border-border/60 px-5 py-4">
          <div className="min-w-0">
            {title ? (
              <h3 className="text-sm font-semibold tracking-tight">{title}</h3>
            ) : null}
            {description ? (
              <p className="mt-0.5 text-sm text-muted-foreground">
                {description}
              </p>
            ) : null}
          </div>

          {action ? <div className="shrink-0">{action}</div> : null}
        </header>
      ) : null}

      <div className={noPadding ? '' : 'px-5 py-5'}>{children}</div>
    </div>
  )
}