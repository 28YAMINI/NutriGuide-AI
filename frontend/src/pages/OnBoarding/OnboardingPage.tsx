import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, Check } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'
import { routePaths } from '../../routes/routePaths'
import { Card } from '../../components/ui/Card'
import { Button } from '../../components/ui/Button'

const STEPS = ['Profile', 'Health', 'Goals', 'Preferences'] as const

export function OnboardingPage() {
    const { user } = useAuth()
    const navigate = useNavigate()
    const [step, setStep] = useState(0)

    const next = () => (step < STEPS.length - 1 ? setStep(step + 1) : navigate(routePaths.dashboard))
    const prev = () => step > 0 && setStep(step - 1)

    return (
        <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6 lg:px-8">
            <h1 className="text-center text-3xl font-bold tracking-tight">Set up your profile</h1>
            <p className="mt-2 text-center text-muted-foreground">A few steps to personalize your nutrition plan.</p>

            {/* Progress bar */}
            <div className="mt-8 flex items-center justify-center gap-2">
                {STEPS.map((s, i) => (
                    <div key={s} className="flex items-center gap-2">
                        <div className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold ${i <= step ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'}`}>
                            {i < step ? <Check className="h-4 w-4" /> : i + 1}
                        </div>
                        {i < STEPS.length - 1 && <div className={`h-0.5 w-12 ${i < step ? 'bg-primary' : 'bg-muted'}`} />}
                    </div>
                ))}
            </div>

            {/* Step content */}
            <Card className="mt-8">
                <div className="py-4 text-center">
                    <p className="text-sm font-medium text-muted-foreground">Step {step + 1} of {STEPS.length}</p>
                    <h2 className="mt-2 text-xl font-semibold">{STEPS[step]}</h2>
                    <p className="mt-2 text-sm text-muted-foreground">
                        {step === 0 && `Welcome, ${user?.firstName ?? 'there'}! Confirm your basic info.`}
                        {step === 1 && 'Add any health conditions or vitals (optional).'}
                        {step === 2 && 'Select your primary goal and activity level.'}
                        {step === 3 && 'Set your diet type and budget preferences.'}
                    </p>
                </div>
            </Card>

            {/* Navigation */}
            <div className="mt-6 flex justify-between">
                <Button variant="outline" onClick={prev} disabled={step === 0}>
                    <ChevronLeft className="mr-1 h-4 w-4" /> Back
                </Button>
                <Button onClick={next}>
                    {step === STEPS.length - 1 ? 'Finish' : 'Next'} <ChevronLeft className="ml-1 h-4 w-4 rotate-180" />
                </Button>
            </div>
        </div>
    )
}