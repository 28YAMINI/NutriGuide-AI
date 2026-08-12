/** Formats grams with one decimal when needed — 12 → "12", 12.5 → "12.5". */
export function formatMacro(value: number): string {
  return Number.isInteger(value) ? String(value) : value.toFixed(1)
}

/** Formats an energy value as kcal — 250.4 → "250 kcal". */
export function formatCalories(calories: number): string {
  return `${Math.round(calories)} kcal`
}