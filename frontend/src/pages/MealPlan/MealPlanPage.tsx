// frontend/src/pages/MealPlan/MealPlanPage.tsx
import { useEffect, useState } from 'react'
import { Sparkles, Utensils, ChevronLeft, ChevronRight, Flame, Beef, Wheat, Droplets } from 'lucide-react'
import { mealPlanService, type MealPlanDetailResponse } from '../../services/mealPlanService'
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
    const [selectedDate, setSelectedDate] = useState(() => {
        const d = new Date()
        return d.toISOString().split('T')[0]
    })

    const loadPlan = async (date: string) => {
        setIsLoading(true)
        setError(null)
        try {
            const result = await mealPlanService.getByDate(date)
            setPlan(result)
        } catch {
            setPlan(null)
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadPlan(selectedDate)
    }, [selectedDate])

    const handleGenerate = async () => {
        setIsGenerating(true)
        setError(null)
        try {
            await mealPlanService.generate({ days: 1, mealsPerDay: 3 })
            await loadPlan(selectedDate)
        } catch (err: unknown) {
            const msg = err instanceof Error ? err.message : 'Failed to generate plan'
            setError(msg)
        } finally {
            setIsGenerating(false)
        }
    }

    const navigateDate = (offset: number) => {
        const d = new Date(selectedDate)
        d.setDate(d.getDate() + offset)
        setSelectedDate(d.toISOString().split('T')[0])
    }

    const formatDate = (dateStr: string) =>
        new Date(dateStr).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })

    if (isLoading) return <MealPlanSkeleton />

    return (
        <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
            {/* Header */}
            <header>
                <h1 className="text-3xl font-bold tracking-tight">Meal Plan</h1>
                <p className="mt-1 text-muted-foreground">AI-generated nutrition plan based on your profile.</p>
            </header>

            {/* Date Navigator */}
            <div className="mt-8 flex items-center justify-center gap-4">
                <Button variant="outline" size="sm" onClick={() => navigateDate(-1)}>
                    <ChevronLeft className="h-4 w-4" />
                </Button>
                <span className="min-w-[200px] text-center text-sm font-medium">{formatDate(selectedDate)}</span>
                <Button variant="outline" size="sm" onClick={() => navigateDate(1)}>
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>

            {/* Generate Button */}
            {!plan && (
                <div className="mt-8">
                    <Card>
                        <div className="flex flex-col items-center gap-4 py-8 text-center">
                            <div className="flex h-14 w-14 items-center justify-center rounded-full bg-primary/10 text-primary">
                                <Utensils className="h-7 w-7" />
                            </div>
                            <div>
                                <h3 className="font-semibold">No meal plan for this day</h3>
                                <p className="mt-1 text-sm text-muted-foreground">Generate a personalized plan based on your health profile.</p>
                            </div>
                            <Button onClick={handleGenerate} disabled={isGenerating}>
                                <Sparkles className="mr-2 h-4 w-4" />
                                {isGenerating ? 'Generating…' : 'Generate Meal Plan'}
                            </Button>
                        </div>
                    </Card>
                    {error && <p className="mt-3 text-center text-sm text-destructive">{error}</p>}
                </div>
            )}

            {/* Plan Display */}
            {plan && (
                <div className="mt-8 space-y-6">
                    {/* Nutrition Targets */}
                    <Card title="Daily Targets" description="The calorie and macro targets this plan was built around.">
                        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                            <div className="text-center">
                                <Flame className="mx-auto h-5 w-5 text-orange-500" />
                                <p className="mt-1 text-lg font-bold">{plan.totalCalories}</p>
                                <p className="text-xs text-muted-foreground">Calories</p>
                            </div>
                            <div className="text-center">
                                <Beef className="mx-auto h-5 w-5 text-red-500" />
                                <p className="mt-1 text-lg font-bold">{Math.round(plan.totalProteinG)}g</p>
                                <p className="text-xs text-muted-foreground">Protein</p>
                            </div>
                            <div className="text-center">
                                <Wheat className="mx-auto h-5 w-5 text-amber-500" />
                                <p className="mt-1 text-lg font-bold">{Math.round(plan.totalCarbsG)}g</p>
                                <p className="text-xs text-muted-foreground">Carbs</p>
                            </div>
                            <div className="text-center">
                                <Droplets className="mx-auto h-5 w-5 text-blue-500" />
                                <p className="mt-1 text-lg font-bold">{Math.round(plan.totalFatG)}g</p>
                                <p className="text-xs text-muted-foreground">Fat</p>
                            </div>
                        </div>
                    </Card>

                    {/* Plan Text */}
                    <Card title="Your Meal Plan" description="AI-generated recommendations for today.">
                        <div className="prose prose-sm max-w-none whitespace-pre-wrap text-foreground">
                            {plan.plan}
                        </div>
                    </Card>

                    {/* Regenerate */}
                    <div className="flex justify-center">
                        <Button variant="outline" onClick={handleGenerate} disabled={isGenerating}>
                            <Sparkles className="mr-2 h-4 w-4" />
                            {isGenerating ? 'Regenerating…' : 'Regenerate Plan'}
                        </Button>
                    </div>
                </div>
            )}
        </div>
    )
}