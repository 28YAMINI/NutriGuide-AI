/**
 * Central food image registry.
 *
 * Imports all category-specific image maps and combines them into a
 * single lookup. Also provides category-level fallback images as
 * inline SVG data URIs — no external SVG files needed.
 */

import { FRUIT_IMAGES } from "./fruits";
import { VEGETABLE_IMAGES } from "./vegetables";
import { GRAIN_IMAGES } from "./grains";
import { DAIRY_IMAGES } from "./dairy";
import { MEAT_IMAGES } from "./meat";
import { SEAFOOD_IMAGES } from "./seafood";
import { LEGUME_IMAGES } from "./legumes";
import { NUT_IMAGES } from "./nuts";
import { BEVERAGE_IMAGES } from "./beverages";
import { SNACK_IMAGES } from "./snacks";

/* ------------------------------------------------------------------ */
/*  Food-specific images (name → inline SVG data URI)                  */
/* ------------------------------------------------------------------ */

/** Combined map of all food names to their inline SVG images. */
const ALL_IMAGES: Record<string, string> = {
    ...FRUIT_IMAGES,
    ...VEGETABLE_IMAGES,
    ...GRAIN_IMAGES,
    ...DAIRY_IMAGES,
    ...MEAT_IMAGES,
    ...SEAFOOD_IMAGES,
    ...LEGUME_IMAGES,
    ...NUT_IMAGES,
    ...BEVERAGE_IMAGES,
    ...SNACK_IMAGES,
};

/**
 * Returns the inline SVG image for a food name, or undefined if
 * not found. Lookup is case-insensitive.
 */
export function getLocalFoodImage(name: string): string | undefined {
    const key = name.trim().toLowerCase();
    return ALL_IMAGES[key];
}

/* ------------------------------------------------------------------ */
/*  Category-level fallback images                                     */
/* ------------------------------------------------------------------ */

function svgDataUri(svg: string): string {
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

function categorySvg(
    emoji: string,
    label: string,
    bgColor: string,
    textColor: string,
): string {
    return svgDataUri(
        `<svg width="400" height="300" viewBox="0 0 400 300" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="400" y2="300" gradientUnits="userSpaceOnUse">
      <stop offset="0" stop-color="${bgColor}"/>
      <stop offset="1" stop-color="${bgColor}cc"/>
    </linearGradient>
  </defs>
  <rect width="400" height="300" rx="12" fill="url(#g)"/>
  <text x="200" y="130" font-size="80" text-anchor="middle" dominant-baseline="central">${emoji}</text>
  <text x="200" y="220" font-size="20" font-family="system-ui,sans-serif" font-weight="600" fill="${textColor}" text-anchor="middle">${label}</text>
</svg>`,
    );
}

const CATEGORY_FALLBACKS: Record<string, string> = {
    FRUITS: categorySvg("🍎", "Fruits", "#dcfce7", "#166534"),
    VEGETABLES: categorySvg("🥦", "Vegetables", "#dcfce7", "#166534"),
    GRAINS: categorySvg("🌾", "Grains", "#fef9c3", "#854d0e"),
    DAIRY: categorySvg("🥛", "Dairy", "#dbeafe", "#1e40af"),
    MEAT: categorySvg("🍗", "Meat", "#fee2e2", "#991b1b"),
    SEAFOOD: categorySvg("🐟", "Seafood", "#dbeafe", "#1e40af"),
    LEGUMES: categorySvg("🫘", "Legumes", "#f3e8ff", "#7e22ce"),
    NUTS_AND_SEEDS: categorySvg("🥜", "Nuts & Seeds", "#fef9c3", "#854d0e"),
    BEVERAGES: categorySvg("🍵", "Beverages", "#d1fae5", "#166534"),
    SNACKS: categorySvg("🍫", "Snacks", "#fee2e2", "#991b1b"),
};

/** Generic fallback for unknown categories. */
export const GENERIC_FALLBACK_IMAGE: string = categorySvg(
    "🥗",
    "Food",
    "#f4f4f5",
    "#525252",
);

/**
 * Returns the category-level fallback SVG for the given category.
 * Falls back to the generic food image if the category is unknown.
 */
export function getCategoryFallbackImage(category: string): string {
    return CATEGORY_FALLBACKS[category] ?? GENERIC_FALLBACK_IMAGE;
}