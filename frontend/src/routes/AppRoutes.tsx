// frontend/src/routes/AppRoutes.tsx
// UPDATE the imports and routes:

import { Route, Routes } from 'react-router-dom'
import { AdminRoute, ProtectedRoute, PublicRoute } from './guards'
import { routePaths } from './routePaths'
import { MainLayout } from '../components/layout/MainLayout'
import { HomePage } from '../pages/Home/HomePage'
import { FoodsPage } from '../pages/Foods/FoodsPage'
import { FoodDetailsPage } from '../pages/FoodDetails/FoodDetailsPage'
import { AboutPage } from '../pages/About/AboutPage'
import { LoginPage } from '../pages/Login/LoginPage'
import { DashboardPage } from '../pages/Dashboard/DashboardPage'
import { RegisterPage } from '../pages/Register/RegisterPage'
import { NotFoundPage } from '../pages/NotFound/NotFoundPage'
import { AdminPage } from '../pages/Admin/AdminPage'
import { ContactPage } from '../pages/Contact/ContactPage'
import { ProfilePage } from '../pages/Profile/ProfilePage'

/* ── new imports ── */
import { MealPlanPage } from '../pages/MealPlan/MealPlanPage'
import {FoodDiaryPage} from "../pages/FoodDiary/FoodDairyPage.tsx";
import {ProgressPage} from "../pages/Progress/ProgressPage.tsx";
import {OnboardingPage} from "../pages/OnBoarding/OnboardingPage.tsx";

export function AppRoutes() {
    // @ts-ignore
    // @ts-ignore
    // @ts-ignore
    return (
        <Routes>
            <Route element={<MainLayout/>}>
                {/* Public */}
                <Route path={routePaths.home} element={<HomePage/>}/>
                <Route path={routePaths.foods} element={<FoodsPage/>}/>
                <Route path={routePaths.foodDetails} element={<FoodDetailsPage/>}/>
                <Route path={routePaths.about} element={<AboutPage/>}/>
                <Route path={routePaths.contact} element={<ContactPage/>}/>

                {/* Auth (signed-out only) */}
                <Route path={routePaths.login} element={<PublicRoute><LoginPage/></PublicRoute>}/>
                <Route path={routePaths.register} element={<PublicRoute><RegisterPage/></PublicRoute>}/>

                {/* Protected (signed-in only) */}
                <Route path={routePaths.dashboard} element={<ProtectedRoute><DashboardPage/></ProtectedRoute>}/>
                <Route path={routePaths.profile} element={<ProtectedRoute><ProfilePage/></ProtectedRoute>}/>
                <Route path={routePaths.mealPlan} element={<ProtectedRoute><MealPlanPage/></ProtectedRoute>}/>
                <Route path={routePaths.foodDiary} element={<ProtectedRoute><FoodDiaryPage/></ProtectedRoute>}/>
                <Route path={routePaths.progress} element={<ProtectedRoute><ProgressPage/></ProtectedRoute>}/>
                <Route path={routePaths.onboarding} element={<ProtectedRoute><OnboardingPage/></ProtectedRoute>}/>

                {/* Admin */}
                <Route path={routePaths.admin} element={<AdminRoute><AdminPage/></AdminRoute>}/>

                {/* Fallback */}
                <Route path="*" element={<NotFoundPage/>}/>
            </Route>
        </Routes>
    )
}