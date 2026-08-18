/**
 * Maps food names to local SVG images in public/images/foods/.
 *
 * Used by FoodImage as a fallback when the remote imageUrl fails
 * to load (offline, CORS, removed, etc.). Category-level fallbacks
 * are defined in constants/food.ts — this layer sits in between,
 * giving each food its own branded illustration.
 */

const LOCAL_IMAGES: Record<string, string> = {
    apple: "/images/foods/apple.svg",
    banana: "/images/foods/banana.svg",
    orange: "/images/foods/orange.svg",
    strawberries: "/images/foods/strawberries.svg",
    blueberries: "/images/foods/blueberries.svg",
    grapes: "/images/foods/grapes.svg",
    mango: "/images/foods/mango.svg",
    avocado: "/images/foods/avocado.svg",
    spinach: "/images/foods/spinach.svg",
    broccoli: "/images/foods/broccoli.svg",
    carrot: "/images/foods/carrot.svg",
    tomato: "/images/foods/tomato.svg",
    "bell pepper": "/images/foods/bell-pepper.svg",
    "boiled potato": "/images/foods/boiled-potato.svg",
    "sweet potato": "/images/foods/sweet-potato.svg",
    onion: "/images/foods/onion.svg",
    oatmeal: "/images/foods/oatmeal.svg",
    "brown rice": "/images/foods/brown-rice.svg",
    quinoa: "/images/foods/quinoa.svg",
    "whole wheat bread": "/images/foods/whole-wheat-bread.svg",
    "whole wheat pasta": "/images/foods/whole-wheat-pasta.svg",
    "milk (2%)": "/images/foods/milk.svg",
    milk: "/images/foods/milk.svg",
    "greek yogurt (plain, nonfat)": "/images/foods/greek-yogurt.svg",
    "greek yogurt": "/images/foods/greek-yogurt.svg",
    "cheddar cheese": "/images/foods/cheddar-cheese.svg",
    cheese: "/images/foods/cheddar-cheese.svg",
    eggs: "/images/foods/eggs.svg",
    "grilled chicken breast": "/images/foods/grilled-chicken-breast.svg",
    chicken: "/images/foods/grilled-chicken-breast.svg",
    "lean ground beef (90%)": "/images/foods/lean-ground-beef.svg",
    "lean ground beef": "/images/foods/lean-ground-beef.svg",
    "salmon fillet": "/images/foods/salmon-fillet.svg",
    salmon: "/images/foods/salmon-fillet.svg",
    "canned tuna (in water)": "/images/foods/canned-tuna.svg",
    "canned tuna": "/images/foods/canned-tuna.svg",
    shrimp: "/images/foods/shrimp.svg",
    lentils: "/images/foods/lentils.svg",
    chickpeas: "/images/foods/chickpeas.svg",
    "black beans": "/images/foods/black-beans.svg",
    almonds: "/images/foods/almonds.svg",
    walnuts: "/images/foods/walnuts.svg",
    "peanut butter": "/images/foods/peanut-butter.svg",
    "green tea": "/images/foods/green-tea.svg",
    "black coffee": "/images/foods/black-coffee.svg",
    coffee: "/images/foods/black-coffee.svg",
    "orange juice (fresh)": "/images/foods/orange-juice.svg",
    "orange juice": "/images/foods/orange-juice.svg",
    "dark chocolate (70%+)": "/images/foods/dark-chocolate.svg",
    "dark chocolate": "/images/foods/dark-chocolate.svg",
    "oatmeal cookie": "/images/foods/oatmeal-cookie.svg",
};

/**
 * Returns the local SVG path for a given food name, or undefined
 * if no match is found. Lookup is case-insensitive.
 */
export function getLocalFoodImage(name: string): string | undefined {
    const key = name.trim().toLowerCase();
    return LOCAL_IMAGES[key];
}