// frontend/src/pages/FoodDiary/FoodDiaryPage.tsx
import { useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, Plus, Trash2, Utensils } from 'lucide-react'
import { diaryService, type FoodDiaryEntryResponse, type MealType, type DailyDiaryResponse } from '../../services/diaryService'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'
import { INPUT_CLASSES } from '../../components/ui/Field'

const MEAL_LABELS: Record<MealType, string> = {
    BREAKFAST: 'Breakfast',
    LUNCH: 'Lunch',
    DINNER: 'Dinner',
    SNACKS: 'Snacks',
}

function DiarySkeleton() {
    return (
        <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
            <div className="animate-pulse space-y-6">
                <div className="h-8 w-48 rounded-lg bg-muted" />
                <div className="h-40 rounded-xl border border-border bg-card" />
                <div className="h-60 rounded-xl border border-border bg-card" />
            </div>
        </div>
    )
}

export function FoodDiaryPage() {
    const [diary, setDiary] = useState<DailyDiaryResponse | null>(null)
    const [isLoading, setIsLoading] = useState(true)
    const [selectedDate, setSelectedDate] = useState(() => new Date().toISOString().split('T')[0])
    const [showAddForm, setShowAddForm] = useState(false)

    const loadDiary = async (date: string) => {
        setIsLoading(true)
        try {
            const result = await diaryService.getEntriesByDate(date)
            setDiary(result)
        } catch {
            setDiary(null)
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadDiary(selectedDate)
    }, [selectedDate])

    const handleDelete = async (id: number) => {
        await diaryService.deleteEntry(id)
        await loadDiary(selectedDate)
    }

    const handleEntryAdded = async () => {
        setShowAddForm(false)
        await loadDiary(selectedDate)
    }

    const navigateDate = (offset: number) => {
        const d = new Date(selectedDate)
        d.setDate(d.getDate() + offset)
        setSelectedDate(d.toISOString().split('T')[0])
    }

    const formatDate = (d: string) =>
        new Date(d).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })

    if (isLoading) return <DiarySkeleton />

    const entries = diary?.entries ?? []
    const grouped = entries.reduce<Record<MealType, FoodDiaryEntryResponse[]>>((acc, e) => {
        ;(acc[e.mealType] ??= []).push(e)
        return acc
    }, { BREAKFAST: [], LUNCH: [], DINNER: [], SNACKS: [] })

    return (
        <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6 lg:px-8">
            <header className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Food Diary</h1>
                    <p className="mt-1 text-muted-foreground">Track what you eat each day.</p>
                </div>
                <Button onClick={() => setShowAddForm(!showAddForm)}>
                    <Plus className="mr-2 h-4 w-4" /> Log food
                </Button>
            </header>

            {/* Date Navigator */}
            <div className="mt-8 flex items-center justify-center gap-4">
                <Button variant="outline" size="sm" onClick={() => navigateDate(-1)}><ChevronLeft className="h-4 w-4" /></Button>
                <span className="min-w-[200px] text-center text-sm font-medium">{formatDate(selectedDate)}</span>
                <Button variant="outline" size="sm" onClick={() => navigateDate(1)}><ChevronRight className="h-4 w-4" /></Button>
            </div>

            {/* Add Entry Form */}
            {showAddForm && (
                <div className="mt-6">
                    <AddEntryForm date={selectedDate} onAdded={handleEntryAdded} onCancel={() => setShowAddForm(false)} />
                </div>
            )}

            {/* Daily Totals */}
            {entries.length > 0 && (
                <Card className="mt-6">
                    <div className="grid grid-cols-4 gap-4 text-center">
                        <div><p className="text-lg font-bold">{Math.round(diary?.totalCalories ?? 0)}</p><p className="text-xs text-muted-foreground">Calories</p></div>
                        <div><p className="text-lg font-bold">{Math.round(diary?.totalProteinG ?? 0)}g</p><p className="text-xs text-muted-foreground">Protein</p></div>
                        <div><p className="text-lg font-bold">{Math.round(diary?.totalCarbsG ?? 0)}g</p><p className="text-xs text-muted-foreground">Carbs</p></div>
                        <div><p className="text-lg font-bold">{Math.round(diary?.totalFatG ?? 0)}g</p><p className="text-xs text-muted-foreground">Fat</p></div>
                    </div>
                </Card>
            )}

            {/* Entries by Meal Type */}
            <div className="mt-6 space-y-4">
                {(Object.keys(grouped) as MealType[]).map((mealType) => {
                    const items = grouped[mealType]
                    if (items.length === 0) return null
                    return (
                        <Card key={mealType} title={MEAL_LABELS[mealType]} description={`${items.length} ${items.length === 1 ? 'entry' : 'entries'}`}>
                            <ul className="divide-y divide-border">
                                {items.map((entry) => (
                                    <li key={entry.id} className="flex items-center justify-between py-3">
                                        <div>
                                            <p className="text-sm font-medium">{entry.foodName}</p>
                                            <p className="text-xs text-muted-foreground">{entry.servingSize} · {Math.round(entry.calories)} kcal</p>
                                        </div>
                                        <button onClick={() => handleDelete(entry.id)} className="text-muted-foreground transition-colors hover:text-destructive" aria-label={`Delete ${entry.foodName}`}>
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        </Card>
                    )
                })}
            </div>

            {entries.length === 0 && (
                <Card className="mt-6">
                    <div className="flex flex-col items-center gap-3 py-8 text-center">
                        <Utensils className="h-8 w-8 text-muted-foreground" />
                        <p className="text-sm text-muted-foreground">No meals logged today. Start tracking!</p>
                    </div>
                </Card>
            )}
        </div>
    )
}

/* ── Add Entry Sub-Form ── */

function AddEntryForm({ date, onAdded, onCancel }: { date: string; onAdded: () => void; onCancel: () => void }) {
    const [foodName, setFoodName] = useState('')
    const [mealType, setMealType] = useState<MealType>('LUNCH')
    const [servingSize, setServingSize] = useState('')
    const [quantity, setQuantity] = useState('1')
    const [calories, setCalories] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setIsSubmitting(true)
        try {
            await diaryService.logEntry({
                foodName,
                mealType,
                servingSize,
                quantity: Number(quantity),
                calories: calories ? Number(calories) : undefined,
                loggedDate: date,
            })
            onAdded()
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <Card title="Log food entry">
            <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid gap-4 sm:grid-cols-2">
                    <div>
                        <label className="text-sm font-medium">Food name</label>
                        <input required value={foodName} onChange={(e) => setFoodName(e.target.value)} placeholder="e.g. Grilled Chicken" className={INPUT_CLASSES} />
                    </div>
                    <div>
                        <label className="text-sm font-medium">Meal type</label>
                        <select value={mealType} onChange={(e) => setMealType(e.target.value as MealType)} className={INPUT_CLASSES}>
                            {(Object.keys(MEAL_LABELS) as MealType[]).map((m) => <option key={m} value={m}>{MEAL_LABELS[m]}</option>)}
                        </select>
                    </div>
                    <div>
                        <label className="text-sm font-medium">Serving size</label>
                        <input required value={servingSize} onChange={(e) => setServingSize(e.target.value)} placeholder="e.g. 1 breast" className={INPUT_CLASSES} />
                    </div>
                    <div>
                        <label className="text-sm font-medium">Quantity</label>
                        <input type="number" min="0.5" step="0.5" value={quantity} onChange={(e) => setQuantity(e.target.value)} className={INPUT_CLASSES} />
                    </div>
                    <div>
                        <label className="text-sm font-medium">Calories (optional)</label>
                        <input type="number" min="0" value={calories} onChange={(e) => setCalories(e.target.value)} placeholder="Auto-filled from catalog" className={INPUT_CLASSES} />
                    </div>
                </div>
                <div className="flex gap-3">
                    <Button type="submit" disabled={isSubmitting}>{isSubmitting ? 'Saving…' : 'Save entry'}</Button>
                    <Button variant="outline" type="button" onClick={onCancel}>Cancel</Button>
                </div>
            </form>
        </Card>
    )
}