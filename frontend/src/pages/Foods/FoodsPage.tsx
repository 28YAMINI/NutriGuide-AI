import { useEffect, useMemo, useState } from 'react'
import { Search, SlidersHorizontal, X } from 'lucide-react'

import type { FoodCategory, FoodItem } from '../../types/food'
import { FoodCard } from '../../components/food/FoodCard'
import { foodService } from '../../services/foodService'
import {
    FOOD_CATEGORIES,
    CATEGORY_LABELS,
} from '../../constants/food'

const CATEGORIES: Array<{
    value: FoodCategory | 'ALL'
    label: string
}> = [
    { value: 'ALL', label: 'All Foods' },
    ...FOOD_CATEGORIES.map((category) => ({
        value: category,
        label: CATEGORY_LABELS[category],
    })),
]

export function FoodsPage() {
    const [foods, setFoods] = useState<FoodItem[]>([])
    const [selectedCategory, setSelectedCategory] =
        useState<FoodCategory | 'ALL'>('ALL')

    const [searchQuery, setSearchQuery] = useState('')
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let cancelled = false

        const loadFoods = async () => {
            try {
                setLoading(true)
                setError(null)

                const data = await foodService.getAllFoods()

                if (!cancelled) {
                    setFoods(data)
                }
            } catch (err) {
                if (!cancelled) {
                    console.error('Failed to load foods:', err)
                    setError('Unable to load foods. Please try again.')
                }
            } finally {
                if (!cancelled) {
                    setLoading(false)
                }
            }
        }

        void loadFoods()

        return () => {
            cancelled = true
        }
    }, [])

    const filteredFoods = useMemo(() => {
        const query = searchQuery.trim().toLowerCase()

        return foods.filter((food) => {
            const matchesCategory =
                selectedCategory === 'ALL' ||
                food.category === selectedCategory

            const matchesSearch =
                !query ||
                food.name.toLowerCase().includes(query) ||
                food.description.toLowerCase().includes(query)

            return matchesCategory && matchesSearch
        })
    }, [foods, selectedCategory, searchQuery])

    const clearFilters = () => {
        setSelectedCategory('ALL')
        setSearchQuery('')
    }

    const hasFilters =
        selectedCategory !== 'ALL' || searchQuery.trim().length > 0

    return (
        <main className="min-h-screen bg-background">
            {/* Header */}
            <section className="border-b border-border bg-background">
                <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
                    <div className="max-w-3xl">
                        <p className="mb-2 text-sm font-medium text-primary">
                            Nutrition Library
                        </p>

                        <h1 className="text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
                            Explore Foods
                        </h1>

                        <p className="mt-3 text-muted-foreground">
                            Discover nutritious foods and explore their calories and
                            nutritional information.
                        </p>
                    </div>

                    {/* Search */}
                    <div className="mt-7 flex flex-col gap-4 lg:flex-row">
                        <div className="relative flex-1">
                            <Search
                                className="pointer-events-none absolute left-3 top-1/2 size-5 -translate-y-1/2 text-muted-foreground"
                                aria-hidden="true"
                            />

                            <input
                                type="search"
                                value={searchQuery}
                                onChange={(event) => setSearchQuery(event.target.value)}
                                placeholder="Search foods..."
                                aria-label="Search foods"
                                className="h-11 w-full rounded-lg border border-border bg-background pl-10 pr-10 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                            />

                            {searchQuery && (
                                <button
                                    type="button"
                                    onClick={() => setSearchQuery('')}
                                    aria-label="Clear search"
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                                >
                                    <X className="size-4" />
                                </button>
                            )}
                        </div>

                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                            <SlidersHorizontal className="size-4" aria-hidden="true" />
                            <span>{filteredFoods.length} foods</span>
                        </div>
                    </div>
                </div>
            </section>

            {/* Category filters */}
            <section className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
                <div className="mb-7 flex gap-2 overflow-x-auto pb-2">
                    {CATEGORIES.map((category) => {
                        const active = selectedCategory === category.value

                        return (
                            <button
                                key={category.value}
                                type="button"
                                onClick={() => setSelectedCategory(category.value)}
                                className={[
                                    'shrink-0 rounded-full px-4 py-2 text-sm font-medium transition-colors',
                                    active
                                        ? 'bg-primary text-primary-foreground'
                                        : 'bg-muted text-muted-foreground hover:bg-muted/80 hover:text-foreground',
                                ].join(' ')}
                            >
                                {category.label}
                            </button>
                        )
                    })}
                </div>

                {/* Active filters */}
                {hasFilters && (
                    <div className="mb-5 flex items-center gap-2">
            <span className="text-sm text-muted-foreground">
              Active filters
            </span>

                        <button
                            type="button"
                            onClick={clearFilters}
                            className="text-sm font-medium text-primary hover:underline"
                        >
                            Clear all
                        </button>
                    </div>
                )}

                {/* Loading */}
                {loading && (
                    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        {Array.from({ length: 8 }).map((_, index) => (
                            <div
                                key={index}
                                className="overflow-hidden rounded-xl border border-border bg-card"
                            >
                                <div className="aspect-[4/3] animate-pulse bg-muted" />

                                <div className="space-y-3 p-4">
                                    <div className="h-5 w-2/3 animate-pulse rounded bg-muted" />
                                    <div className="h-3 w-1/3 animate-pulse rounded bg-muted" />
                                    <div className="h-10 w-full animate-pulse rounded bg-muted" />
                                    <div className="h-10 w-full animate-pulse rounded bg-muted" />
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {/* Error */}
                {!loading && error && (
                    <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-8 text-center">
                        <h2 className="font-semibold text-foreground">
                            Something went wrong
                        </h2>

                        <p className="mt-2 text-sm text-muted-foreground">
                            {error}
                        </p>

                        <button
                            type="button"
                            onClick={() => window.location.reload()}
                            className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground"
                        >
                            Try again
                        </button>
                    </div>
                )}

                {/* Empty */}
                {!loading && !error && filteredFoods.length === 0 && (
                    <div className="rounded-xl border border-border bg-card p-10 text-center">
                        <h2 className="font-semibold text-foreground">
                            No foods found
                        </h2>

                        <p className="mt-2 text-sm text-muted-foreground">
                            Try a different search term or category.
                        </p>

                        {hasFilters && (
                            <button
                                type="button"
                                onClick={clearFilters}
                                className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground"
                            >
                                Clear filters
                            </button>
                        )}
                    </div>
                )}

                {/* Food cards */}
                {!loading && !error && filteredFoods.length > 0 && (
                    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                        {filteredFoods.map((food) => (
                            <FoodCard key={food.id} food={food} />
                        ))}
                    </div>
                )}
            </section>
        </main>
    )
}