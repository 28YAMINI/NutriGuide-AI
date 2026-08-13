import type { ReactNode } from 'react'

/**
 * Shared styling for inputs, selects, and textareas — apply it via
 * className so every control gets the same height, border, focus ring,
 * placeholder, and disabled treatment.
 */
export const INPUT_CLASSES =
  'h-10 w-full rounded-lg border border-input bg-background px-3 text-sm ' +
  'text-foreground placeholder:text-muted-foreground/60 transition-colors ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ' +
  'focus-visible:ring-offset-1 focus-visible:ring-offset-background ' +
  'disabled:cursor-not-allowed disabled:opacity-60'

interface FieldProps {
  label?: ReactNode
  /** id of the control this label/error belongs to. */
  htmlFor?: string
  /** Validation error message shown under the control. */
  error?: string
  /** Helpful description shown under the control. */
  hint?: string
  /** Renders a required asterisk after the label. */
  required?: boolean
  className?: string
  children: ReactNode
}

/**
 * Form field wrapper: label, control, and hint/error message with
 * stable ids (htmlFor + "-error" / "-hint") for aria-describedby wiring.
 */
export function Field({
  label,
  htmlFor,
  error,
  hint,
  required = false,
  className = '',
  children,
}: FieldProps) {
  return (
    <div className={`space-y-1.5 ${className}`.trim()}>
      {label ? (
        <label
          htmlFor={htmlFor}
          className="block text-sm font-medium text-foreground"
        >
          {label}
          {required ? (
            <span aria-hidden="true" className="ml-0.5 text-destructive">
              *
            </span>
          ) : null}
        </label>
      ) : null}

      {children}

      {error ? (
        <p
          id={htmlFor ? `${htmlFor}-error` : undefined}
          role="alert"
          className="text-xs font-medium text-destructive"
        >
          {error}
        </p>
      ) : hint ? (
        <p
          id={htmlFor ? `${htmlFor}-hint` : undefined}
          className="text-xs text-muted-foreground"
        >
          {hint}
        </p>
      ) : null}
    </div>
  )
}