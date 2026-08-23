// File: src/pages/MealPlanPage.tsx

import type { MealPlanDetailResponse, MealPlanFocus, MealPlanRequest } from "../../types/mealPlan.ts";
import { useEffect, useState } from "react";
import { mealPlanService } from "../../services/mealPlanService.ts";

const FOCUS_OPTIONS: { id: MealPlanFocus; label: string; desc: string; icon: string }[] = [
    { id: 'DAILY', label: 'Standard Daily', desc: 'Balanced everyday nutrition', icon: '🥗' },
    { id: 'HIGH_PROTEIN', label: 'High Protein', desc: 'Muscle building & recovery', icon: '🥩' },
    { id: 'LOW_CARB', label: 'Low Carb', desc: 'Lower carbs & clean fats', icon: '🥑' },
    { id: 'BALANCED', label: 'Macro Balanced', desc: 'Optimal ratio of all macros', icon: '⚖️' },
    { id: 'BUDGET_FRIENDLY', label: 'Budget Friendly', desc: 'Accessible simple ingredients', icon: '🛒' },
];

export function MealPlanPage() {
    const [days, setDays] = useState<number>(3);
    const [mealsPerDay, setMealsPerDay] = useState<number>(3);
    const [focus, setFocus] = useState<MealPlanFocus>('DAILY');

    const [loading, setLoading] = useState<boolean>(false);
    const [initialLoading, setInitialLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [mealPlan, setMealPlan] = useState<MealPlanDetailResponse | null>(null);

    const getTodayISO = () => new Date().toISOString().split('T')[0];

    // 1. On component load, check if today's meal plan already exists in database
    useEffect(() => {
        let isMounted = true;

        const fetchTodayPlan = async () => {
            try {
                const today = getTodayISO();
                const existing = await mealPlanService.getByDate(today);
                if (isMounted && existing) {
                    setMealPlan(existing);
                }
            } catch {
                // Gracefully ignore - no plan for today yet
            } finally {
                if (isMounted) {
                    setInitialLoading(false);
                }
            }
        };

        // Explicitly invoke and catch to satisfy linter rule: "Promise returned from fetchTodayPlan is ignored"
        void fetchTodayPlan();

        return () => {
            isMounted = false;
        };
    }, []);

    // 2. Handler to generate new plan
    const handleGenerate = async () => {
        try {
            setLoading(true);
            setError(null);

            const payload: MealPlanRequest = {
                days: Number(days),
                mealsPerDay: Number(mealsPerDay),
                focus,
            };

            const result = await mealPlanService.generate(payload);
            setMealPlan(result);
        } catch (err: any) {
            const status = err?.response?.status;
            const resMsg = err?.response?.data?.message || err?.message || '';

            // If backend threw DuplicatePlanException, fetch today's plan instead
            if (status === 409 || resMsg.toLowerCase().includes('already exists')) {
                setError('A meal plan for today already exists! Displaying today’s plan below.');
                try {
                    const existing = await mealPlanService.getByDate(getTodayISO());
                    if (existing) {
                        setMealPlan(existing);
                    }
                } catch {
                    // ignore
                }
            } else {
                setError(resMsg || 'Failed to generate meal plan. Please check backend AI configuration.');
            }
        } finally {
            setLoading(false);
        }
    };

    if (initialLoading) {
        return (
            <div className="flex items-center justify-center p-12 text-gray-500 font-medium">
                Checking today's meal plan...
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto p-6 space-y-8">
            {/* Header */}
            <div>
                <h1 className="text-2xl font-bold text-gray-900">AI Meal Plan Generator</h1>
                <p className="text-sm text-gray-500 mt-1">
                    Customize duration, meal count, and dietary focus powered by your AI Nutrition Assistant.
                </p>
            </div>

            {/* Info / Error Banner */}
            {error && (
                <div className="p-4 bg-amber-50 border border-amber-200 text-amber-800 rounded-xl text-sm font-medium">
                    {error}
                </div>
            )}

            {/* Options Form */}
            <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-6">

                {/* 1. Days (1 to 7) */}
                <div>
                    <label className="block text-sm font-semibold text-gray-800 mb-2">
                        1. Select Number of Days (1 – 7 Days)
                    </label>
                    <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-7 gap-2">
                        {[1, 2, 3, 4, 5, 6, 7].map((num) => (
                            <button
                                key={num}
                                type="button"
                                onClick={() => setDays(num)}
                                className={`py-3 text-center border rounded-xl font-medium transition-all ${
                                    days === num
                                        ? 'border-emerald-600 bg-emerald-50 text-emerald-900 ring-2 ring-emerald-500/20 font-bold'
                                        : 'border-gray-200 hover:border-gray-300 text-gray-700'
                                }`}
                            >
                                {num} {num === 1 ? 'Day' : 'Days'}
                            </button>
                        ))}
                    </div>
                </div>

                {/* 2. Meals Per Day (2 to 6) */}
                <div>
                    <label className="block text-sm font-semibold text-gray-800 mb-2">
                        2. Select How Many Meals per Day (2 – 6 Meals)
                    </label>
                    <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
                        {[
                            { count: 2, label: '2 Meals', sub: 'Brunch & Dinner' },
                            { count: 3, label: '3 Meals', sub: 'Standard 3 Meals' },
                            { count: 4, label: '4 Meals', sub: '3 Meals + 1 Snack' },
                            { count: 5, label: '5 Meals', sub: '3 Meals + 2 Snacks' },
                            { count: 6, label: '6 Meals', sub: 'High Frequency' },
                        ].map((opt) => (
                            <button
                                key={opt.count}
                                type="button"
                                onClick={() => setMealsPerDay(opt.count)}
                                className={`p-3 text-left border rounded-xl transition-all ${
                                    mealsPerDay === opt.count
                                        ? 'border-emerald-600 bg-emerald-50 text-emerald-900 ring-2 ring-emerald-500/20 font-semibold'
                                        : 'border-gray-200 hover:border-gray-300'
                                }`}
                            >
                                <div className="text-sm font-semibold">{opt.label}</div>
                                <div className="text-xs text-gray-500 mt-0.5">{opt.sub}</div>
                            </button>
                        ))}
                    </div>
                </div>

                {/* 3. Nutrition Focus */}
                <div>
                    <label className="block text-sm font-semibold text-gray-800 mb-2">
                        3. Select Nutrition Focus
                    </label>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                        {FOCUS_OPTIONS.map((opt) => (
                            <button
                                key={opt.id}
                                type="button"
                                onClick={() => setFocus(opt.id)}
                                className={`p-3.5 text-left border rounded-xl flex items-start gap-3 transition-all ${
                                    focus === opt.id
                                        ? 'border-emerald-600 bg-emerald-50/60 ring-2 ring-emerald-500/20'
                                        : 'border-gray-200 hover:border-gray-300'
                                }`}
                            >
                                <span className="text-2xl">{opt.icon}</span>
                                <div>
                                    <div className="font-semibold text-sm text-gray-900">{opt.label}</div>
                                    <div className="text-xs text-gray-500 mt-0.5">{opt.desc}</div>
                                </div>
                            </button>
                        ))}
                    </div>
                </div>

                {/* Submit */}
                <div className="pt-3 border-t border-gray-100 flex items-center justify-between">
                    <div className="text-xs text-gray-500">
                        Selected: <strong>{days} Days</strong> • <strong>{mealsPerDay} Meals/Day</strong> • Focus: <strong>{focus}</strong>
                    </div>
                    <button
                        type="button"
                        onClick={handleGenerate}
                        disabled={loading}
                        className="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold text-sm rounded-xl transition-all shadow"
                    >
                        {loading ? 'Generating with AI...' : '✨ Generate AI Meal Plan'}
                    </button>
                </div>
            </div>

            {/* Meal Plan Display */}
            {mealPlan && (
                <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-6">
                    <div className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-100 pb-4">
                        <div>
                            <span className="text-xs font-semibold uppercase tracking-wider text-emerald-600">Active Plan</span>
                            <h2 className="text-xl font-bold text-gray-900">Meal Plan for {mealPlan.planDate}</h2>
                        </div>
                        <div className="flex gap-4 text-xs font-medium text-gray-600">
                            <div className="bg-gray-50 px-3 py-1.5 rounded-lg border">
                                <strong>{mealPlan.totalCalories}</strong> kcal
                            </div>
                            <div className="bg-gray-50 px-3 py-1.5 rounded-lg border">
                                Protein: <strong>{mealPlan.totalProtein}g</strong>
                            </div>
                            <div className="bg-gray-50 px-3 py-1.5 rounded-lg border">
                                Carbs: <strong>{mealPlan.totalCarbs}g</strong>
                            </div>
                            <div className="bg-gray-50 px-3 py-1.5 rounded-lg border">
                                Fat: <strong>{mealPlan.totalFat}g</strong>
                            </div>
                        </div>
                    </div>

                    {/* Render Markdown / AI Plan Content */}
                    <div className="prose max-w-none text-gray-800 text-sm whitespace-pre-wrap leading-relaxed font-sans bg-gray-50/50 p-5 rounded-xl border border-gray-100">
                        {mealPlan.plan}
                    </div>
                </div>
            )}
        </div>
    );
}