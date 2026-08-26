const FOOD_IMAGES: Record<string, string> = {
    almonds: '/images/foods/almonds.jpg',
    apple: '/images/foods/apple.jpg',
    banana: '/images/foods/banana-1025109_1280.jpg',
    'whole wheat bread': '/images/foods/bread.jpg',
    bread: '/images/foods/bread.jpg',
    broccoli: '/images/foods/broccoli.jpg',
    carrot: '/images/foods/carrot.jpg',
    cheese: '/images/foods/cheese.jpg',
    chicken: '/images/foods/chicken.jpg',
    'grilled chicken breast': '/images/foods/chicken.jpg',
    chickpeas: '/images/foods/chickpeas.jpg',
    'coconut oil': '/images/foods/coconut oil.jpg',
    coffee: '/images/foods/coffee.jpg',
    'black coffee': '/images/foods/coffee.jpg',
    curd: '/images/foods/Curd.jpg',
    eggs: '/images/foods/eggs.jpg',
    fish: '/images/foods/fish.jpg',
    salmon: '/images/foods/fish.jpg',
    'salmon fillet': '/images/foods/fish.jpg',
    grapes: '/images/foods/grapes.jpg',
    'green tea': '/images/foods/greentea.jpg',
    lentils: '/images/foods/lentils.jpg',
    mango: '/images/foods/mango.jpg',
    milk: '/images/foods/milk.jpg',
    'milk (2%)': '/images/foods/milk.jpg',
    oatmeal: '/images/foods/oats.jpg',
    oats: '/images/foods/oats.jpg',
    orange: '/images/foods/orange.jpg',
    paneer: '/images/foods/panner.jpg',
    'peanut butter': '/images/foods/peasnuts.jpg',
    peanuts: '/images/foods/peasnuts.jpg',
    potato: '/images/foods/potato.jpg',
    'boiled potato': '/images/foods/potato.jpg',
    rice: '/images/foods/rice.jpg',
    'brown rice': '/images/foods/rice.jpg',
    spinach: '/images/foods/spinach.jpg',
    tomato: '/images/foods/tomato.jpg',
    turmeric: '/images/foods/turmeric.jpg',
}

const CATEGORY_IMAGES: Record<string, string> = {
    FRUITS: '/images/foods/apple.jpg',
    VEGETABLES: '/images/foods/broccoli.jpg',
    GRAINS: '/images/foods/rice.jpg',
    DAIRY: '/images/foods/milk.jpg',
    MEAT: '/images/foods/chicken.jpg',
    SEAFOOD: '/images/foods/fish.jpg',
    LEGUMES: '/images/foods/lentils.jpg',
    NUTS_AND_SEEDS: '/images/foods/almonds.jpg',
    BEVERAGES: '/images/foods/coffee.jpg',
    SNACKS: '/images/foods/peasnuts.jpg',
}

const GENERIC_IMAGE = '/images/foods/apple.jpg'

export function getFoodImage(name: string): string | undefined {
    return FOOD_IMAGES[name.trim().toLowerCase()]
}

export function getCategoryImage(category: string): string {
    return CATEGORY_IMAGES[category] ?? GENERIC_IMAGE
}