/**
 * Inline SVG food images as data URIs.
 * No external files needed — everything lives in this one file.
 * Each food gets a colored gradient background + emoji + name.
 */

function svgDataUri(svg: string): string {
    return `data:image/svg+xml,${encodeURIComponent(svg)}`;
}

const svg = (bg1: string, bg2: string, emoji: string, name: string, textColor: string) =>
    svgDataUri(
        `<svg width="400" height="300" viewBox="0 0 400 300" xmlns="http://www.w3.org/2000/svg">
  <defs><linearGradient id="g" x1="0" y1="0" x2="400" y2="300" gradientUnits="userSpaceOnUse">
    <stop offset="0" stop-color="${bg1}"/><stop offset="1" stop-color="${bg2}"/></linearGradient></defs>
  <rect width="400" height="300" rx="12" fill="url(#g)"/>
  <text x="200" y="150" font-size="80" text-anchor="middle" dominant-baseline="central">${emoji}</text>
  <text x="200" y="220" font-size="18" font-family="system-ui,sans-serif" font-weight="600" fill="${textColor}" text-anchor="middle">${name}</text>
</svg>`
    );

const GENERIC = svg("#f2f4f0", "#e1e5dc", "🥗", "Food", "#525252");

const CATEGORY: Record<string, string> = {
    FRUITS: svg("#f3fbf6", "#dcf2e5", "🍎", "Fruits", "#166534"),
    VEGETABLES: svg("#f3fbf6", "#dcf2e5", "🥦", "Vegetables", "#166534"),
    GRAINS: svg("#faf6ea", "#f0e5c6", "🌾", "Grains", "#854d0e"),
    DAIRY: svg("#f2f8fb", "#dcecf5", "🥛", "Dairy", "#1e40af"),
    MEAT: svg("#fdf1ef", "#f7dcd6", "🍗", "Meat", "#991b1b"),
    SEAFOOD: svg("#eef4fb", "#d4e6f7", "🐟", "Seafood", "#1e40af"),
    LEGUMES: svg("#f3f1fb", "#e2ddf5", "🫘", "Legumes", "#7e22ce"),
    NUTS_AND_SEEDS: svg("#faf6ea", "#f0e5c6", "🥜", "Nuts & Seeds", "#854d0e"),
    BEVERAGES: svg("#eefbf6", "#d6f3e4", "🍵", "Beverages", "#166534"),
    SNACKS: svg("#fef2f2", "#fecaca", "🍫", "Snacks", "#991b1b"),
};

const FOOD_IMAGES: Record<string, string> = {
    apple: svg("#fef2f2", "#fecaca", "🍎", "Apple", "#991b1b"),
    banana: svg("#fefce8", "#fef08a", "🍌", "Banana", "#854d0e"),
    orange: svg("#fff7ed", "#fed7aa", "🍊", "Orange", "#9a3412"),
    strawberries: svg("#fdf2f8", "#fbcfe8", "🍓", "Strawberries", "#9d174d"),
    blueberries: svg("#eef2ff", "#c7d2fe", "🫐", "Blueberries", "#3730a3"),
    grapes: svg("#faf5ff", "#e9d5ff", "🍇", "Grapes", "#7e22ce"),
    mango: svg("#fffbeb", "#fde68a", "🥭", "Mango", "#92400e"),
    avocado: svg("#f0fdf4", "#bbf7d0", "🥑", "Avocado", "#166534"),
    spinach: svg("#f0fdf4", "#86efac", "🥬", "Spinach", "#166534"),
    broccoli: svg("#f0fdf4", "#86efac", "🥦", "Broccoli", "#166534"),
    carrot: svg("#fff7ed", "#fdba74", "🥕", "Carrot", "#9a3412"),
    tomato: svg("#fef2f2", "#fca5a5", "🍅", "Tomato", "#991b1b"),
    "bell pepper": svg("#fef2f2", "#fde047", "🫑", "Bell Pepper", "#854d0e"),
    "boiled potato": svg("#fefce8", "#fde68a", "🥔", "Boiled Potato", "#854d0e"),
    "sweet potato": svg("#fff7ed", "#fdba74", "🍠", "Sweet Potato", "#9a3412"),
    onion: svg("#fefce8", "#fef9c3", "🧅", "Onion", "#854d0e"),
    oatmeal: svg("#fefce8", "#fde68a", "🥣", "Oatmeal", "#854d0e"),
    "brown rice": svg("#fefce8", "#fef9c3", "🍚", "Brown Rice", "#854d0e"),
    quinoa: svg("#fefce8", "#fde68a", "🌾", "Quinoa", "#854d0e"),
    "whole wheat bread": svg("#fefce8", "#fde68a", "🍞", "Whole Wheat Bread", "#854d0e"),
    "whole wheat pasta": svg("#fefce8", "#fde68a", "🍝", "Whole Wheat Pasta", "#854d0e"),
    "milk (2%)": svg("#eff6ff", "#bfdbfe", "🥛", "Milk (2%)", "#1e40af"),
    milk: svg("#eff6ff", "#bfdbfe", "🥛", "Milk", "#1e40af"),
    "greek yogurt (plain, nonfat)": svg("#eff6ff", "#bfdbfe", "🫙", "Greek Yogurt", "#1e40af"),
    "greek yogurt": svg("#eff6ff", "#bfdbfe", "🫙", "Greek Yogurt", "#1e40af"),
    "cheddar cheese": svg("#fefce8", "#fef08a", "🧀", "Cheddar Cheese", "#854d0e"),
    cheese: svg("#fefce8", "#fef08a", "🧀", "Cheese", "#854d0e"),
    eggs: svg("#fefce8", "#fef9c3", "🥚", "Eggs", "#854d0e"),
    "grilled chicken breast": svg("#fef2f2", "#fecaca", "🍗", "Grilled Chicken", "#991b1b"),
    chicken: svg("#fef2f2", "#fecaca", "🍗", "Chicken", "#991b1b"),
    "lean ground beef (90%)": svg("#fef2f2", "#fecaca", "🥩", "Lean Ground Beef", "#991b1b"),
    "lean ground beef": svg("#fef2f2", "#fecaca", "🥩", "Lean Ground Beef", "#991b1b"),
    "salmon fillet": svg("#eff6ff", "#93c5fd", "🐟", "Salmon Fillet", "#1e40af"),
    salmon: svg("#eff6ff", "#93c5fd", "🐟", "Salmon", "#1e40af"),
    "canned tuna (in water)": svg("#eff6ff", "#93c5fd", "🐟", "Canned Tuna", "#1e40af"),
    "canned tuna": svg("#eff6ff", "#93c5fd", "🐟", "Canned Tuna", "#1e40af"),
    shrimp: svg("#eff6ff", "#93c5fd", "🦐", "Shrimp", "#1e40af"),
    lentils: svg("#fdf4ff", "#e9d5ff", "🫘", "Lentils", "#7e22ce"),
    chickpeas: svg("#fdf4ff", "#e9d5ff", "🫘", "Chickpeas", "#7e22ce"),
    "black beans": svg("#1c1917", "#44403c", "🫘", "Black Beans", "#d6d3d1"),
    almonds: svg("#fefce8", "#fde68a", "🥜", "Almonds", "#854d0e"),
    walnuts: svg("#fefce8", "#fde68a", "🥜", "Walnuts", "#854d0e"),
    "peanut butter": svg("#fefce8", "#fde68a", "🫙", "Peanut Butter", "#854d0e"),
    "green tea": svg("#f0fdf4", "#bbf7d0", "🍵", "Green Tea", "#166534"),
    "black coffee": svg("#1c1917", "#44403c", "☕", "Black Coffee", "#d6d3d1"),
    coffee: svg("#1c1917", "#44403c", "☕", "Coffee", "#d6d3d1"),
    "orange juice (fresh)": svg("#fff7ed", "#fed7aa", "🍊", "Orange Juice", "#9a3412"),
    "orange juice": svg("#fff7ed", "#fed7aa", "🍊", "Orange Juice", "#9a3412"),
    "dark chocolate (70%+)": svg("#1c1917", "#78350f", "🍫", "Dark Chocolate", "#fef3c7"),
    "dark chocolate": svg("#1c1917", "#78350f", "🍫", "Dark Chocolate", "#fef3c7"),
    "oatmeal cookie": svg("#fefce8", "#fde68a", "🍪", "Oatmeal Cookie", "#854d0e"),
};

export function getFoodImage(name: string): string | undefined {
    return FOOD_IMAGES[name.trim().toLowerCase()];
}

export function getCategoryImage(category: string): string {
    return CATEGORY[category] ?? GENERIC;
}