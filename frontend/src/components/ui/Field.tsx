import type { ReactNode } from 'react'

/** Shared input styling for every form control in the app. */
export const INPUT_CLASSES =
  'w-full rounded-lg border border-border bg-background px-3.5 py-2.5 text-sm text-foreground placeholder:text-muted-foreground transition-colors focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/30 aria-invalid:border-red-500 aria-invalid:focus:ring-red-500/30 disabled:cursor-not-allowed disabled:opacity-60'

interface FieldProps {
  label: string
  htmlFor: string
  error?: string
  children: ReactNode
}

/**
 * Label + control + inline validation message.
 *
 * The error message has the stable id `${htmlFor}-error` so pages can
 * pass aria-describedby to the control (see AdminPage for the pattern).
 */
export function Field({ label, htmlFor, error, children }: FieldProps) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={htmlFor} className="block text-sm font-medium text-foreground">
        {label}
      </label>
      {children}
      {error ? (
        <p id={`${htmlFor}-error`} role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}
    </div>
  )
}