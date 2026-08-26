import axios from 'axios'
import { getToken, clearTokens } from '../utils/token'

/**
 * Custom event dispatched when an authenticated request receives
 * a final 401 response.
 */
export const AUTH_UNAUTHORIZED_EVENT = 'nutriguide:unauthorized'

/**
 * Axios instance configured for the NutriGuide AI backend.
 *
 * - Base URL: /api (proxied by Vite in dev, nginx in prod)
 * - Request interceptor: attaches JWT from token.ts
 * - Response interceptor: handles authentication and server errors
 */
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
})

// ─── Request Interceptor ─────────────────────────────────────────────────────

api.interceptors.request.use(
    (config) => {
        const token = getToken()

        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    },
    (error) => Promise.reject(error),
)

// ─── Response Interceptor ────────────────────────────────────────────────────

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const { response } = error

        if (!response) {
            console.error('[API] Network error — no response received')
            return Promise.reject(error)
        }

        const { status } = response

        switch (status) {
            case 401: {
                console.warn(
                    '[API] 401 Unauthorized — clearing authentication state'
                )

                clearTokens()

                window.dispatchEvent(
                    new CustomEvent(AUTH_UNAUTHORIZED_EVENT)
                )

                if (!window.location.pathname.startsWith('/login')) {
                    window.location.href = '/login'
                }

                break
            }

            case 403: {
                console.warn(
                    '[API] 403 Forbidden — insufficient permissions'
                )
                break
            }

            case 500: {
                console.error('[API] 500 Internal Server Error')
                break
            }

            default:
                break
        }

        return Promise.reject(error)
    },
)

/**
 * Bare axios instance without the authentication interceptor.
 *
 * Used for refresh and logout operations.
 */
const rawApi = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
})

export { rawApi }

export default api