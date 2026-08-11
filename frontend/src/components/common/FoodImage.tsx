import { useState } from 'react'
import { Utensils } from 'lucide-react'

interface FoodImageProps {
  /** Backend-provided image URL (food.imageUrl). */
  src?: string | null
  /** Meaningful description for screen readers. */
  alt: string
  /** Wrapper classes — pass the aspect ratio here, e.g. "aspect-square". */
  className?: string
  /** Image element classes (object-cover, rounding, etc.). */
  imgClassName?: string
  loading?: 'lazy' | 'eager'
}

/**
 * Food image with graceful degradation.
 *
 * Lazy-loads the backend imageUrl inside a fixed-aspect container and
 * falls back to a branded placeholder when the URL is missing or fails
 * to load — so a broken image can never break the card layout.
 */
export function FoodImage({
  src,
  alt,
  className = '',
  imgClassName = '',
  loading = 'lazy',
}: FoodImageProps) {
  const [failed, setFailed] = useState(false)
  const hasImage = Boolean(src) && !failed

  return (
    <div
      className={`relative aspect-[4/3] overflow-hidden rounded-lg bg-muted ${className}`.trim()}
    >
      {hasImage && src ? (
        <img
          key={src}
          src={src}
          alt={alt}
          loading={loading}
          onError={() => setFailed(true)}
          className={`h-full w-full object-cover ${imgClassName}`.trim()}
        />
      ) : (
        <span className="absolute inset-0 flex items-center justify-center text-muted-foreground/60">
          <Utensils aria-hidden="true" className="h-8 w-8" />
        </span>
      )}
    </div>
  )
}