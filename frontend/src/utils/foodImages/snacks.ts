/**
 * Food images for the SNACKS category.
 *
 * To add a new snack, just add a new entry to the map.
 */

function svgDataUri(svg: string): string {
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

function snackSvg(
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
  <text x="200" y="220" font-size="18" font-family="system-ui,sans-serif" font-weight="600" fill="#991b1b" text-anchor="middle">${name}</text>
</svg>`,
    );
}

export const SNACK_IMAGES: Record<string, string> = {
    "dark chocolate (70%+)": snackSvg("#fef2f2", "#fecaca", "🍫", "Dark Chocolate"),
    "oatmeal cookie": snackSvg("#fefce8", "#fef08a", "🍪", "Oatmeal Cookie"),
};