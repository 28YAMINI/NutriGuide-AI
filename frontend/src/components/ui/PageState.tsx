import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

interface PageStateProps {
  icon: LucideIcon
  title: string
  message: string
  /** Optional action rendered below the message (button or link). */
  action?: ReactNode
  className?: string
}

/** Centered icon + title + message + optional action for error/empty states. */
export function PageState({
  icon: Icon,
  title,
  message,
  action,
  className = '',
}: PageStateProps) {
  return (
    <div
      className={`flex flex-col items-center justify-center rounded-xl border border-dashed border-border bg-card px-6 py-14 text-center ${className}`.trim()}
    >
      <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary">
        <Icon aria-hidden="true" className="h-6 w-6" />
      </span>
      <h2 className="mt-4 text-lg font-semibold text-foreground">{title}</h2>
      <p className="mt-1 max-w-sm text-sm text-muted-foreground">{message}</p>
      {action ? <div className="mt-6">{action}</div> : null}
    </div>
  )
}