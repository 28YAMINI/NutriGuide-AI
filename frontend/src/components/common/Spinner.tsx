const SIZE_CLASSES = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-8 w-8',
} as const

export type SpinnerSize = keyof typeof SIZE_CLASSES

interface SpinnerProps {
  /** Size variant — sm for buttons/inline, lg for full-page loading. */
  size?: SpinnerSize
  /** Accessible name announced to screen readers while loading. */
  label?: string
  /** Extra classes for positioning (e.g. centering inside a container). */
  className?: string
}

/**
 * Reusable loading indicator.
 *
 * Purely visual — callers handle their own layout (centering a page,
 * sizing a button) so this component stays composable.
 */
export function Spinner({
  size = 'md',
  label = 'Loading…',
  className = '',
}: SpinnerProps) {
  return (
    <span
      role="status"
      aria-live="polite"
      className={`inline-flex items-center justify-center ${className}`}
    >
      <span
        aria-hidden="true"
        className={`animate-spin rounded-full border-2 border-foreground/20 border-t-foreground ${SIZE_CLASSES[size]}`}
      />
      <span className="sr-only">{label}</span>
    </span>
  )
}