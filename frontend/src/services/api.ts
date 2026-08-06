import axios from 'axios'
import type { AxiosError } from 'axios'


import { getToken } from '../utils/token'
import type { ApiErrorResponse } from '../types/api.types'

/** Fired when a protected request returns 401 — AuthContext listens and logs out. */
export const AUTH_UNAUTHORIZED_EVENT = 'auth:unauthorized'

/**
 * Shared axios instance for all API calls.
 *
 * The request interceptor attaches the JWT when present. The response
 * interceptor centralizes error handling: 401s on protected endpoints
 * signal an expired/invalid session; every rejection is unwrapped via
 * getApiErrorMessage().
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
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

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorResponse>) => {
    const status = error.response?.status

    // Login/register legitimately return 401 (bad credentials) — don't
    // treat those as a session expiry.
    const isAuthRequest = error.config?.url?.includes('/auth/') ?? false

    if (status === 401 && !isAuthRequest) {
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