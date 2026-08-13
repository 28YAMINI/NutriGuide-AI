import type { ButtonHTMLAttributes } from 'react'

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'destructive'
type ButtonSize = 'sm' | 'md' | 'lg'

const BASE_CLASSES =
  'inline-flex cursor-pointer items-center justify-center gap-2 rounded-lg font-medium transition ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ' +
  'focus-visible:ring-offset-2 focus-visible:ring-offset-background ' +
  'active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60'

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary:
    'bg-primary text-primary-foreground shadow-sm hover:bg-primary/90 active:bg-primary/95',
  secondary:
    'bg-secondary text-secondary-foreground hover:bg-secondary/80 active:bg-secondary/90',
  outline:
    'border border-border bg-background text-foreground hover:bg-muted active:bg-muted/80',
  ghost: 'text-muted-foreground hover:bg-muted hover:text-foreground',
  destructive:
    'bg-destructive text-destructive-foreground shadow-sm hover:bg-destructive/90 active:bg-destructive/95',
}

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'h-8 px-3 text-xs',
  md: 'h-10 px-4 text-sm',
  lg: 'h-11 px-5 text-base',
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant
  size?: ButtonSize
  /** Shows an inline spinner and disables the button while true. */
  isLoading?: boolean
}

/**
 * Shared button primitive.
 *
 * Provides consistent variants, sizes, keyboard focus styles, press
 * feedback, disabled behavior, and loading state across the app.
 */
export function Button({
  variant = 'primary',
  size = 'md',
  isLoading = false,
  className = '',
  type = 'button',
  disabled,
  children,
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      aria-busy={isLoading || undefined}
      className={`${BASE_CLASSES} ${VARIANT_CLASSES[variant]} ${SIZE_CLASSES[size]} ${className}`.trim()}
      {...props}
    >
      {isLoading ? (
        <span
          aria-hidden="true"
          className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"
        />
      ) : null}

      {children}
    </button>
  )
}

/**
 * Same visual classes as Button, for elements that should look like
 * buttons without rendering a <button> (e.g. a router <Link>).
 */
export function buttonClassName(
  variant: ButtonVariant = 'primary',
  size: ButtonSize = 'md',
  className = '',
): string {
  return `${BASE_CLASSES} ${VARIANT_CLASSES[variant]} ${SIZE_CLASSES[size]} ${className}`.trim()
}