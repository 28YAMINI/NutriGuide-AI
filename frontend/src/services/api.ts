import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'

import {
    clearTokens,
    getRefreshToken,
    getToken,
    setRefreshToken,
    setToken,
} from '../utils/token'
import type { ApiErrorResponse } from '../types/api.types'
import type { RefreshResponse } from '../types/auth'

/** Fired when a refresh attempt fails — AuthContext listens and logs out. */
export const AUTH_UNAUTHORIZED_EVENT = 'auth:unauthorized'

/**
 * Shared axios instance for all API calls.
 *
 * The request interceptor attaches the access token when present. The
 * response interceptor centralizes error handling: a 401 on a protected
 * endpoint triggers a single silent refresh (shared across concurrent
 * requests); if the refresh fails, the session ends via
 * AUTH_UNAUTHORIZED_EVENT.
 */
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081/api',
    timeout: 10_000,
    headers: { 'Content-Type': 'application/json' },
})

/**
 * Bare instance for auth endpoints that must NOT run through the
 * interceptors — otherwise a failed refresh would loop forever.
 */
export const rawApi = axios.create({
    baseURL: api.defaults.baseURL,
    timeout: 10_000,
    headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
    const token = getToken()

    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }

    return config
})

/** One refresh at a time — concurrent 401s share the same promise. */
let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken(): Promise<string | null> {
    const refreshToken = getRefreshToken()
    if (!refreshToken) return null

    refreshPromise ??= rawApi
        .post<RefreshResponse>('/auth/refresh', { refreshToken })
        .then(({ data }) => {
            setToken(data.token)
            setRefreshToken(data.refreshToken)
            return data.token
        })
        .catch(() => {
            clearTokens()
            return null
        })
        .finally(() => {
            refreshPromise = null
        })

    return refreshPromise
}

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean }

api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError<ApiErrorResponse>) => {
        const original = error.config as RetriableConfig | undefined
        const status = error.response?.status

        // Login/register legitimately return 401 (bad credentials) — don't
        // treat those as a session expiry.
        const isAuthRequest = error.config?.url?.includes('/auth/') ?? false

        if (status === 401 && !isAuthRequest && original && !original._retried) {
            original._retried = true

            const newToken = await refreshAccessToken()
            if (newToken) {
                original.headers.Authorization = `Bearer ${newToken}`
                return api(original)
            }

            window.dispatchEvent(new Event(AUTH_UNAUTHORIZED_EVENT))
        }

        return Promise.reject(error)
    },
)

/** Extracts a readable message from any failed request. */
export function getApiErrorMessage(error: unknown): string {
    if (axios.isAxiosError<ApiErrorResponse>(error)) {
        return error.response?.data?.message ?? error.message
    }

    return error instanceof Error ? error.message : 'Something went wrong'
}

export default api