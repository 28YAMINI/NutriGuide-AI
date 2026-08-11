import type { ReactNode } from 'react'
import type { LucideIcon } from 'lucide-react'

interface PageStateProps {
  /** Icon for the state (spinner, search, alert, etc.). */
  icon?: LucideIcon
  title: string
  message?: string
  /** Optional CTA / retry button. */
  action?: ReactNode
  className?: string
}

/**
 * Centered placeholder for loading, empty, and error states.
 *
 * Consumers pick the icon and pass a retry or CTA as the action so the
 * state always offers a next step instead of a dead end.
 */
export function PageState({
  icon: Icon,
  title,
  message,
  action,
  className = '',
}: PageStateProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center px-6 py-16 text-center ${className}`.trim()}
    >
      {Icon ? (
        <span className="flex h-12 w-12 items-center justify-center rounded-full bg-muted text-muted-foreground">
          <Icon aria-hidden="true" className="h-6 w-6" />
        </span>
      ) : null}

      <h2 className="mt-4 text-lg font-semibold tracking-tight">{title}</h2>

      {message ? (
        <p className="mt-1.5 max-w-sm text-sm text-muted-foreground">
          {message}
        </p>
      ) : null}

      {action ? <div className="mt-6">{action}</div> : null}
    </div>
  )
}