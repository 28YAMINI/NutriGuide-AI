import { useState } from 'react'
import type { FoodCategory } from '../../types/food'
import { getFoodImage, getCategoryImage } from '../../utils/foodImages'

interface FoodImageProps {
    src?: string | null
    alt: string
    category: FoodCategory
    className?: string
}

export function FoodImage({
                              src,
                              alt,
                              category,
                              className = '',
                          }: FoodImageProps) {
    const [hasError, setHasError] = useState(false)

    const localImage = getFoodImage(alt)

    const imageSrc =
        !hasError && (src || localImage)
            ? src || localImage
            : getCategoryImage(category)

    return (
        <img
            src={imageSrc}
            alt={alt}
            className={`h-full w-full object-cover ${className}`}
            onError={() => setHasError(true)}
        />
    )
}