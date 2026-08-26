/**
 * Food images for the FRUITS category.
 *
 * Each food maps to an inline SVG data URI — gradient background,
 * emoji icon, and food name. No external files needed.
 *
 * To add a new fruit, just add a new entry to the map.
 */

function svgDataUri(svg: string): string {
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

function fruitSvg(
    bg1: string,
    bg2: string,
    emoji: string,
    name: string,
): string {
    return svgDataUri(
        `<svg width="400" height="300" viewBox="0 0 400 300" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="400" y2="300" gradientUnits="userSpaceOnUse">
      <stop offset="0" stop-color="${bg1}"/>
      <stop offset="1" stop-color="${bg2}"/>
    </linearGradient>
  </defs>
  <rect width="400" height="300" rx="12" fill="url(#g)"/>
  <text x="200" y="140" font-size="80" text-anchor="middle" dominant-baseline="central">${emoji}</text>
  <text x="200" y="220" font-size="18" font-family="system-ui,sans-serif" font-weight="600" fill="#166534" text-anchor="middle">${name}</text>
</svg>`,
    );
}

export const FRUIT_IMAGES: Record<string, string> = {
    apple: fruitSvg("#fef2f2", "#fecaca", "🍎", "Apple"),
    banana: fruitSvg("#fefce8", "#fef08a", "🍌", "Banana"),
    orange: fruitSvg("#fff7ed", "#fed7aa", "🍊", "Orange"),
    strawberries: fruitSvg("#fdf2f8", "#fbcfe8", "🍓", "Strawberries"),
    blueberries: fruitSvg("#eef2ff", "#c7d2fe", "🫐", "Blueberries"),
    grapes: fruitSvg("#faf5ff", "#e9d5ff", "🍇", "Grapes"),
    mango: fruitSvg("#fffbeb", "#fde68a", "🥭", "Mango"),
    avocado: fruitSvg("#f0fdf4", "#bbf7d0", "🥑", "Avocado"),
};