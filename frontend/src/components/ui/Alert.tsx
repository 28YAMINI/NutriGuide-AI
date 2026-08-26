import type { ReactNode } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  Info,
  X,
  XCircle,
  type LucideIcon,
} from 'lucide-react'

type AlertTone = 'info' | 'success' | 'warning' | 'error'

const TONE_CLASSES: Record<AlertTone, string> = {
  info: 'border-sky-200 bg-sky-50 text-sky-700 dark:border-sky-900 dark:bg-sky-950/50 dark:text-sky-400',
  success:
    'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-900 dark:bg-emerald-950/50 dark:text-emerald-400',
  warning:
    'border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-900 dark:bg-amber-950/50 dark:text-amber-400',
  error:
    'border-red-200 bg-red-50 text-red-700 dark:border-red-900 dark:bg-red-950/50 dark:text-red-400',
}

const TONE_ICONS: Record<AlertTone, LucideIcon> = {
  info: Info,
  success: CheckCircle2,
  warning: AlertTriangle,
  error: XCircle,
}

interface AlertProps {
  tone?: AlertTone
  /** Optional bold heading above the message body. */
  title?: ReactNode
  children: ReactNode
  className?: string
  /** When provided, renders a dismiss button. */
  onDismiss?: () => void
}

/**
 * Inline status banner. Errors use role="alert" so assistive tech
 * announces them; all other tones use role="status".
 */
export function Alert({
  tone = 'info',
  title,
  children,
  className = '',
  onDismiss,
}: AlertProps) {
  const Icon = TONE_ICONS[tone]

  return (
    <div
      role={tone === 'error' ? 'alert' : 'status'}
      className={`flex items-start gap-3 rounded-lg border px-4 py-3 text-sm ${TONE_CLASSES[tone]} ${className}`.trim()}
    >
      <Icon aria-hidden="true" className="mt-0.5 h-4 w-4 shrink-0" />

      <div className="min-w-0 flex-1">
        {title ? <p className="font-semibold">{title}</p> : null}
        {children}
      </div>

      {onDismiss ? (
        <button
          type="button"
          onClick={onDismiss}
          aria-label="Dismiss"
          className="shrink-0 rounded-md p-0.5 transition-colors hover:bg-black/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-current dark:hover:bg-white/10"
        >
          <X aria-hidden="true" className="h-4 w-4" />
        </button>
      ) : null}
    </div>
  )
}