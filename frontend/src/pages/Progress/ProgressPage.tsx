// frontend/src/pages/Progress/ProgressPage.tsx
import { useEffect, useState } from 'react'
import {
    TrendingDown,
    TrendingUp,
    Minus,
    Save
} from 'lucide-react'
import { progressService, type DashboardSummary, type WeightTrend, type CalorieTrend, type MacroBreakdown } from '../../services/progressService'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'
import { INPUT_CLASSES } from '../../components/ui/Field'

function ProgressSkeleton() {
    return (
        <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
            <div className="animate-pulse space-y-6">
                <div className="h-8 w-48 rounded-lg bg-muted" />
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">{[0,1,2,3].map(i => <div key={i} className="h-32 rounded-xl border border-border bg-card" />)}</div>
                <div className="grid gap-4 lg:grid-cols-2">{[0,1].map(i => <div key={i} className="h-56 rounded-xl border border-border bg-card" />)}</div>
            </div>
        </div>
    )
}

const TrendIcon = ({ dir }: { dir: string }) => {
    if (dir === 'DOWN') return <TrendingDown className="h-4 w-4 text-green-600" />
    if (dir === 'UP') return <TrendingUp className="h-4 w-4 text-orange-500" />
    return <Minus className="h-4 w-4 text-muted-foreground" />
}

export function ProgressPage() {
    const [summary, setSummary] = useState<DashboardSummary | null>(null)
    const [weightTrend, setWeightTrend] = useState<WeightTrend | null>(null)
    const [calorieTrend, setCalorieTrend] = useState<CalorieTrend | null>(null)
    const [macros, setMacros] = useState<MacroBreakdown | null>(null)
    const [isLoading, setIsLoading] = useState(true)

    // Tracking form
    const [weight, setWeight] = useState('')
    const [water, setWater] = useState('')
    const [sleep, setSleep] = useState('')
    const [isSaving, setIsSaving] = useState(false)

    useEffect(() => {
        const load = async () => {
            setIsLoading(true)
            try {
                const [s, w, c, m] = await Promise.all([
                    progressService.getSummary(),
                    progressService.getWeightTrend(30),
                    progressService.getCalorieTrend(7),
                    progressService.getMacros(),
                ])
                setSummary(s)
                setWeightTrend(w)
                setCalorieTrend(c)
                setMacros(m)
            } catch { /* ignore */ } finally {
                setIsLoading(false)
            }
        }
        load()
    }, [])

    const handleTrack = async (e: React.FormEvent) => {
        e.preventDefault()
        setIsSaving(true)
        try {
            await progressService.trackDaily({
                weightKg: weight ? Number(weight) : undefined,
                waterIntakeMl: water ? Number(water) : undefined,
                sleepHours: sleep ? Number(sleep) : undefined,
            })
            setWeight('')
            setWater('')
            setSleep('')
            // Reload summary
            const s = await progressService.getSummary()
            setSummary(s)
            const w = await progressService.getWeightTrend(30)
            setWeightTrend(w)
        } finally {
            setIsSaving(false)
        }
    }

    if (isLoading) return <ProgressSkeleton />

    const maxCalories = Math.max(...(calorieTrend?.dataPoints.map(p => Math.max(p.consumed, p.target)) ?? [2000]))

    return (
        <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
            <h1 className="text-3xl font-bold tracking-tight">Progress</h1>
            <p className="mt-1 text-muted-foreground">Track your health journey over time.</p>

            {/* Summary Cards */}
            <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <Card>
                    <p className="text-xs text-muted-foreground">Weight</p>
                    <p className="mt-1 text-2xl font-bold">{summary?.currentWeightKg ? `${summary.currentWeightKg} kg` : '—'}</p>
                    <div className="mt-1 flex items-center gap-1"><TrendIcon dir={weightTrend?.trendDirection ?? 'STABLE'} /><span className="text-xs text-muted-foreground">{weightTrend?.trendDirection ?? 'No data'}</span></div>
                </Card>
                <Card>
                    <p className="text-xs text-muted-foreground">BMI</p>
                    <p className="mt-1 text-2xl font-bold">{summary?.bmi?.toFixed(1) ?? '—'}</p>
                </Card>
                <Card>
                    <p className="text-xs text-muted-foreground">Calories today</p>
                    <p className="mt-1 text-2xl font-bold">{summary?.caloriesConsumedToday ?? 0}</p>
                    <p className="text-xs text-muted-foreground">Target: {summary?.targetCalories ?? '—'}</p>
                </Card>
                <Card>
                    <p className="text-xs text-muted-foreground">Streak</p>
                    <p className="mt-1 text-2xl font-bold">{summary?.activeStreakDays ?? 0} days</p>
                </Card>
            </div>

            {/* Calorie Trend Bar Chart */}
            {calorieTrend && (
                <Card title="Calorie trend" description="Last 7 days — consumed vs target" className="mt-8">
                    <div className="flex items-end gap-2" style={{ height: 180 }}>
                        {calorieTrend.dataPoints.map((p) => {
                            const consumedH = maxCalories > 0 ? (p.consumed / maxCalories) * 160 : 0
                            const targetH = maxCalories > 0 ? (p.target / maxCalories) * 160 : 0
                            const day = new Date(p.date).toLocaleDateString('en-US', { weekday: 'short' })
                            return (
                                <div key={p.date} className="flex flex-1 flex-col items-center gap-1">
                                    <div className="flex w-full gap-0.5" style={{ height: 160 }}>
                                        <div className="flex-1 rounded-t bg-primary" style={{ height: consumedH }} title={`${p.consumed} kcal`} />
                                        <div className="flex-1 rounded-t bg-muted" style={{ height: targetH }} title={`Target: ${p.target}`} />
                                    </div>
                                    <span className="text-[10px] text-muted-foreground">{day}</span>
                                </div>
                            )
                        })}
                    </div>
                    <div className="mt-3 flex justify-center gap-4 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-primary" /> Consumed</span>
                        <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-muted" /> Target</span>
                    </div>
                </Card>
            )}

            {/* Macro Breakdown */}
            {macros && (
                <Card title="Macro breakdown" description="Today's protein, carbs, and fat vs target" className="mt-8">
                    <div className="space-y-4">
                        {([
                            { label: 'Protein', consumed: macros.proteinConsumedG, target: macros.proteinTargetG, pct: macros.proteinPercent, color: 'bg-red-500' },
                            { label: 'Carbs', consumed: macros.carbsConsumedG, target: macros.carbsTargetG, pct: macros.carbsPercent, color: 'bg-amber-500' },
                            { label: 'Fat', consumed: macros.fatConsumedG, target: macros.fatTargetG, pct: macros.fatPercent, color: 'bg-blue-500' },
                        ]).map((m) => (
                            <div key={m.label}>
                                <div className="flex justify-between text-sm"><span>{m.label}</span><span className="text-muted-foreground">{Math.round(m.consumed)}g / {Math.round(m.target)}g</span></div>
                                <div className="mt-1 h-2.5 overflow-hidden rounded-full bg-muted">
                                    <div className={`h-full rounded-full ${m.color}`} style={{ width: `${Math.min(m.pct, 100)}%` }} />
                                </div>
                            </div>
                        ))}
                    </div>
                </Card>
            )}

            {/* Daily Tracking Form */}
            <Card title="Log today's tracking" description="Record weight, water intake, and sleep." className="mt-8">
                <form onSubmit={handleTrack} className="space-y-4">
                    <div className="grid gap-4 sm:grid-cols-3">
                        <div>
                            <label className="text-sm font-medium">Weight (kg)</label>
                            <input type="number" step="0.1" value={weight} onChange={(e) => setWeight(e.target.value)} placeholder={summary?.currentWeightKg?.toString() ?? '75.0'} className={INPUT_CLASSES} />
                        </div>
                        <div>
                            <label className="text-sm font-medium">Water (ml)</label>
                            <input type="number" value={water} onChange={(e) => setWater(e.target.value)} placeholder="2500" className={INPUT_CLASSES} />
                        </div>
                        <div>
                            <label className="text-sm font-medium">Sleep (hours)</label>
                            <input type="number" step="0.5" value={sleep} onChange={(e) => setSleep(e.target.value)} placeholder="7.5" className={INPUT_CLASSES} />
                        </div>
                    </div>
                    <Button type="submit" disabled={isSaving}><Save className="mr-2 h-4 w-4" />{isSaving ? 'Saving…' : 'Save today'}</Button>
                </form>
            </Card>
        </div>
    )
}