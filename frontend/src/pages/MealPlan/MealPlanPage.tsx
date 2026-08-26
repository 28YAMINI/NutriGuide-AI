import { useEffect, useState } from 'react'
import {
    Sparkles,
    Utensils,
    ChevronLeft,
    ChevronRight,
    Flame,
    Beef,
    Wheat,
    Droplets,
} from 'lucide-react'

import {
    mealPlanService,
    type MealPlanDetailResponse,
    type MealPlanFocus,
} from '../../services/mealPlanService'

import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'

function MealPlanSkeleton() {
    return (
        <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
            <div className="animate-pulse space-y-6">
                <div className="h-8 w-48 rounded-lg bg-muted" />
                <div className="h-12 w-full rounded-xl bg-muted" />
                <div className="h-64 rounded-xl border border-border bg-card" />
            </div>
        </div>
    )
}

export function MealPlanPage() {
    const [plan, setPlan] = useState<MealPlanDetailResponse | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [isGenerating, setIsGenerating] = useState(false)
    const [error, setError] = useState<string | null>(null)

    const [days, setDays] = useState(1)
    const [mealsPerDay, setMealsPerDay] = useState(3)
    const [focus, setFocus] = useState<MealPlanFocus>('DAILY')

    const [selectedDate, setSelectedDate] = useState(() => {
        const d = new Date()
        return d.toISOString().split('T')[0]
    })

    const loadPlan = async (date: string) => {
        setIsLoading(true)
        setError(null)

        await mealPlanService
            .getByDate(date)
            .then((result) => {
                setPlan(result)
            })
            .catch(() => {
                setPlan(null)
            })
            .finally(() => {
                setIsLoading(false)
            })
    }

    useEffect(() => {
        void loadPlan(selectedDate)
    }, [selectedDate])

    const handleGenerate = async () => {
        setIsGenerating(true)
        setError(null)

        await mealPlanService
            .generate({
                days,
                mealsPerDay,
                focus,
            })
            .then((result) => {
                setPlan(result)
            })
            .catch((err: unknown) => {
                const msg =
                    err instanceof Error
                        ? err.message
                        : 'Failed to generate meal plan'

                setError(msg)
            })
            .finally(() => {
                setIsGenerating(false)
            })
    }

    const navigateDate = (offset: number) => {
        const d = new Date(selectedDate)
        d.setDate(d.getDate() + offset)
        setSelectedDate(d.toISOString().split('T')[0])
    }

    const formatDate = (dateStr: string) =>
        new Date(dateStr).toLocaleDateString('en-US', {
            weekday: 'long',
            month: 'long',
            day: 'numeric',
            year: 'numeric',
        })

    if (isLoading) {
        return <MealPlanSkeleton />
    }

    return (
        <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
            {/* Header */}
            <header>
                <h1 className="text-3xl font-bold tracking-tight">
                    Meal Plan
                </h1>

                <p className="mt-1 text-muted-foreground">
                    AI-generated nutrition plan based on your profile.
                </p>
            </header>

            {/* Date Navigator */}
            <div className="mt-8 flex items-center justify-center gap-4">
                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigateDate(-1)}
                    aria-label="Previous day"
                >
                    <ChevronLeft className="h-4 w-4" />
                </Button>

                <span className="min-w-[200px] text-center text-sm font-medium">
                    {formatDate(selectedDate)}
                </span>

                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigateDate(1)}
                    aria-label="Next day"
                >
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>

            {/* AI Plan Controls */}
            <Card
                className="mt-8"
                title="Create your plan"
                description="Choose how you want NutriGuide AI to build your meal plan."
            >
                <div className="grid gap-4 sm:grid-cols-3">
                    {/* Days */}
                    <div>
                        <label
                            htmlFor="meal-plan-days"
                            className="text-sm font-medium"
                        >
                            Days
                        </label>

                        <select
                            id="meal-plan-days"
                            value={days}
                            onChange={(e) =>
                                setDays(Number(e.target.value))
                            }
                            disabled={isGenerating}
                            className="mt-1.5 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                        >
                            {[1, 2, 3, 4, 5, 6, 7].map((value) => (
                                <option key={value} value={value}>
                                    {value} {value === 1 ? 'day' : 'days'}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Meals per day */}
                    <div>
                        <label
                            htmlFor="meal-plan-meals"
                            className="text-sm font-medium"
                        >
                            Meals per day
                        </label>

                        <select
                            id="meal-plan-meals"
                            value={mealsPerDay}
                            onChange={(e) =>
                                setMealsPerDay(Number(e.target.value))
                            }
                            disabled={isGenerating}
                            className="mt-1.5 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                        >
                            {[2, 3, 4, 5, 6].map((value) => (
                                <option key={value} value={value}>
                                    {value} meals
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* Focus */}
                    <div>
                        <label
                            htmlFor="meal-plan-focus"
                            className="text-sm font-medium"
                        >
                            Planning focus
                        </label>

                        <select
                            id="meal-plan-focus"
                            value={focus}
                            onChange={(e) =>
                                setFocus(e.target.value as MealPlanFocus)
                            }
                            disabled={isGenerating}
                            className="mt-1.5 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground outline-none focus:ring-2 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                        >
                            <option value="DAILY">Daily nutrition</option>
                            <option value="WEEKLY">Weekly planning</option>
                            <option value="GROCERY">Grocery focused</option>
                        </select>
                    </div>
                </div>

                {/* Generate */}
                <div className="mt-5 flex justify-center">
                    <Button
                        onClick={() => void handleGenerate()}
                        disabled={isGenerating}
                    >
                        <Sparkles className="mr-2 h-4 w-4" />

                        {isGenerating
                            ? 'Generating…'
                            : plan
                                ? 'Regenerate Meal Plan'
                                : 'Generate Meal Plan'}
                    </Button>
                </div>

                {error && (
                    <p className="mt-3 text-center text-sm text-destructive">
                        {error}
                    </p>
                )}
            </Card>

            {/* No Plan */}
            {!plan && !error && (
                <Card className="mt-8">
                    <div className="flex flex-col items-center gap-4 py-8 text-center">
                        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-primary/10 text-primary">
                            <Utensils className="h-7 w-7" />
                        </div>

                        <div>
                            <h3 className="font-semibold">
                                No meal plan for this day
                            </h3>

                            <p className="mt-1 text-sm text-muted-foreground">
                                Choose your preferences above and generate a
                                personalized plan.
                            </p>
                        </div>
                    </div>
                </Card>
            )}

            {/* Plan Display */}
            {plan && (
                <div className="mt-8 space-y-6">
                    {/* Nutrition Targets */}
                    <Card
                        title="Daily Targets"
                        description="The calorie and macro targets this plan was built around."
                    >
                        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                            <div className="text-center">
                                <Flame className="mx-auto h-5 w-5 text-orange-500" />

                                <p className="mt-1 text-lg font-bold">
                                    {plan.totalCalories}
                                </p>

                                <p className="text-xs text-muted-foreground">
                                    Calories
                                </p>
                            </div>

                            <div className="text-center">
                                <Beef className="mx-auto h-5 w-5 text-red-500" />

                                <p className="mt-1 text-lg font-bold">
                                    {Math.round(plan.totalProteinG)}g
                                </p>

                                <p className="text-xs text-muted-foreground">
                                    Protein
                                </p>
                            </div>

                            <div className="text-center">
                                <Wheat className="mx-auto h-5 w-5 text-amber-500" />

                                <p className="mt-1 text-lg font-bold">
                                    {Math.round(plan.totalCarbsG)}g
                                </p>

                                <p className="text-xs text-muted-foreground">
                                    Carbs
                                </p>
                            </div>

                            <div className="text-center">
                                <Droplets className="mx-auto h-5 w-5 text-blue-500" />

                                <p className="mt-1 text-lg font-bold">
                                    {Math.round(plan.totalFatG)}g
                                </p>

                                <p className="text-xs text-muted-foreground">
                                    Fat
                                </p>
                            </div>
                        </div>
                    </Card>

                    {/* Plan Text */}
                    <Card
                        title="Your Meal Plan"
                        description="AI-generated recommendations based on your profile and preferences."
                    >
                        <div className="prose prose-sm max-w-none whitespace-pre-wrap text-foreground">
                            {plan.plan}
                        </div>
                    </Card>
                </div>
            )}
        </div>
    )
}