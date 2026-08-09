import type { ReactNode } from 'react'

import { X } from 'lucide-react'

type AlertTone = 'success' | 'error'

const TONE_CLASSES: Record<AlertTone, string> = {
  success:
    'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-400',
  error:
    'border-red-200 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-400',
}

interface AlertProps {
  tone: AlertTone
  children: ReactNode
  className?: string
  /** When provided, renders a dismiss button. */
  onDismiss?: () => void
}

/**
 * Inline status banner. Success uses role="status", error uses
 * role="alert" so assistive tech announces them appropriately.
 */
export function Alert({
  tone,
  children,
  className = '',
  onDismiss,
}: AlertProps) {
  return (
    <div
      role={tone === 'success' ? 'status' : 'alert'}
      className={`flex items-start justify-between gap-4 rounded-lg border px-4 py-3 text-sm ${TONE_CLASSES[tone]} ${className}`.trim()}
    >
      <div className="min-w-0 flex-1">{children}</div>

      {onDismiss ? (
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss"
          className="shrink-0 rounded-md p-1 transition-colors hover:bg-black/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-current dark:hover:bg-white/10"
        >
          <X aria-hidden="true" className="h-4 w-4" />
        </button>
      ) : null}
    </div>
  )
}