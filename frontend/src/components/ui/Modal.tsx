import { useEffect, useId, useRef } from 'react'
import { createPortal } from 'react-dom'
import type { KeyboardEvent, ReactNode } from 'react'
import { X } from 'lucide-react'

type ModalSize = 'sm' | 'md' | 'lg'

const SIZE_CLASSES: Record<ModalSize, string> = {
  sm: 'max-w-md',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
}

interface ModalProps {
  open: boolean
  onClose: () => void
  /** Dialog heading — used as the accessible name. */
  title?: string
  /** Optional muted line under the title. */
  description?: string
  /** Action buttons rendered in the sticky footer. */
  footer?: ReactNode
  size?: ModalSize
  /** Close when clicking the backdrop. Defaults to true. */
  closeOnBackdrop?: boolean
  children: ReactNode
}

function ModalContent({
  onClose,
  title,
  description,
  footer,
  size = 'md',
  closeOnBackdrop = true,
  children,
}: ModalProps) {
  const titleId = useId()
  const dialogRef = useRef<HTMLDivElement>(null)

  // Focus the first control, lock body scroll, restore focus on close.
  useEffect(() => {
    const previousFocus = document.activeElement as HTMLElement | null
    const firstFocusable = dialogRef.current?.querySelector<HTMLElement>(
      'button:not([disabled]), a[href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
    )
    ;(firstFocusable ?? dialogRef.current)?.focus()

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'

    return () => {
      document.body.style.overflow = previousOverflow
      previousFocus?.focus()
    }
  }, [])

  // Close on Escape.
  useEffect(() => {
    const onKeyDown = (event: globalThis.KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  // Keep Tab focus within the dialog.
  const handleKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key !== 'Tab') return

    const focusables = dialogRef.current?.querySelectorAll<HTMLElement>(
      'button:not([disabled]), a[href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
    )
    if (!focusables || focusables.length === 0) return

    const first = focusables[0]
    const last = focusables[focusables.length - 1]

    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault()
      last.focus()
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault()
      first.focus()
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      {/* Backdrop */}
      <div
        aria-hidden="true"
        onClick={closeOnBackdrop ? onClose : undefined}
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
      />

      {/* Dialog */}
      <div
        ref={dialogRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-label={title ? undefined : 'Dialog'}
        onKeyDown={handleKeyDown}
        className={`relative z-10 flex max-h-[calc(100dvh-2rem)] w-full flex-col rounded-t-2xl border border-border bg-card text-card-foreground shadow-xl sm:m-4 sm:rounded-2xl ${SIZE_CLASSES[size]}`}
      >
        {title || description ? (
          <header className="flex items-start justify-between gap-4 border-b border-border px-5 py-4">
            <div className="min-w-0">
              {title ? (
                <h2 id={titleId} className="text-base font-semibold tracking-tight">
                  {title}
                </h2>
              ) : null}
              {description ? (
                <p className="mt-0.5 text-sm text-muted-foreground">
                  {description}
                </p>
              ) : null}
            </div>

            <button
              type="button"
              onClick={onClose}
              aria-label="Close dialog"
              className="shrink-0 rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            >
              <X aria-hidden="true" className="h-4 w-4" />
            </button>
          </header>
        ) : (
          <button
            type="button"
            onClick={onClose}
            aria-label="Close dialog"
            className="absolute right-4 top-4 z-10 rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
          >
            <X aria-hidden="true" className="h-4 w-4" />
          </button>
        )}

        <div className="overflow-y-auto px-5 py-4">{children}</div>

        {footer ? (
          <footer className="flex items-center justify-end gap-2 border-t border-border px-5 py-4">
            {footer}
          </footer>
        ) : null}
      </div>
    </div>
  )
}

/**
 * Accessible modal dialog rendered in a portal.
 *
 * Handles Escape-to-close, backdrop close, focus trapping, body scroll
 * lock, and focus restore. Docks to the bottom like a sheet on mobile,
 * centers on larger screens.
 */
export function Modal({ open, ...props }: ModalProps) {
  if (!open) return null

  return createPortal(<ModalContent open={false} {...props} />, document.body)
}