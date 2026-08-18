import axios from 'axios'

/**
 * Custom event dispatched when a 401 response survives the refresh
 * attempt. AuthContext listens for this to clear user state.
 */
export const AUTH_UNAUTHORIZED_EVENT = 'nutriguide:unauthorized'

/**
 * Axios instance configured for the NutriGuide AI backend.
 *
 * - Base URL: /api (proxied by Vite in dev, nginx in prod)
 * - Request interceptor: attaches JWT from localStorage
 * - Response interceptor: handles 401/403/500 errors
 */
const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
})

// ─── Request Interceptor ─────────────────────────────────────────────────────
// Attaches JWT token to every request automatically.
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    },
)

// ─── Response Interceptor ────────────────────────────────────────────────────
// Handles authentication errors and other common error responses.
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const { response } = error

        // No response (network error)
        if (!response) {
            console.error('[API] Network error — no response received')
            return Promise.reject(error)
        }

        const { status } = response

        switch (status) {
            case 401: {
                // Token expired or invalid — clear auth and redirect to login
                console.warn('[API] 401 Unauthorized — clearing token and redirecting to login')
                localStorage.removeItem('accessToken')
                localStorage.removeItem('refreshToken')

                // Dispatch custom event so AuthContext can clear user state
                window.dispatchEvent(new CustomEvent(AUTH_UNAUTHORIZED_EVENT))

                // Only redirect if not already on login page
                if (!window.location.pathname.startsWith('/login')) {
                    window.location.href = '/login'
                }
                break
            }

            case 403: {
                // Forbidden — user doesn't have permission
                console.warn('[API] 403 Forbidden — insufficient permissions')
                break
            }

            case 500: {
                // Server error
                console.error('[API] 500 Internal Server Error')
                break
            }

            default:
                // Other errors (400, 404, 409, etc.) — let the calling code handle them
                break
        }

        return Promise.reject(error)
    },
)

/**
 * Bare axios instance WITHOUT interceptors.
 *
 * Used for token refresh and logout so a failed refresh can never
 * loop back into the 401 interceptor and redirect to /login.
 */
const rawApi = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
})

export { rawApi }
export default api