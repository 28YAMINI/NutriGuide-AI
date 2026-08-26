import { useState } from "react";
import { getFoodImage, getCategoryImage } from "../../utils/foodImages";
import type {FoodCategory} from "../../types/food.ts";

interface FoodImageProps {
    src?: string | null
    alt: string
    category: FoodCategory
    className?: string
    loading?: 'lazy' | 'eager'
}

/**
 * Food image with inline SVG fallbacks.
 *
 * Tries remote URL first, then falls back to a per-food inline SVG,
 * then a category-level SVG. Everything is self-contained — no
 * external image files needed.
 */
export function FoodImage({
                              src,
                              alt,
                              category,
                              className = "",
                              loading = "lazy", }: FoodImageProps) {
  const categoryFallback = getCategoryImage(category);
  const foodFallback = getFoodImage(alt) ?? categoryFallback;
  const initial = src && src.trim() !== "" ? src : foodFallback;

  const [displaySrc, setDisplaySrc] = useState(initial);
  const [prevInitial, setPrevInitial] = useState(initial);

  if (prevInitial !== initial) {
    setPrevInitial(initial);
    setDisplaySrc(initial);
  }

  return (
      <img
          src={displaySrc}
          alt={alt}
          loading={loading}
          decoding="async"
          onError={() => {
            if (displaySrc === initial && displaySrc !== foodFallback) {
              setDisplaySrc(foodFallback);
            } else if (displaySrc !== categoryFallback) {
              setDisplaySrc(categoryFallback);
            }
          }}
          className={className ?? ""}

      />

  );
}