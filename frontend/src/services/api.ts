// File: src/services/api.ts

import axios, {AxiosError, type InternalAxiosRequestConfig,} from 'axios';

export const AUTH_UNAUTHORIZED_EVENT = 'auth:unauthorized';

const BASE_URL = '/api';

/**
 * Robust token finder from all standard frontend storage locations
 */
export function getStoredToken(): string | null {
    if (typeof window === 'undefined') return null;

    // 1. Check direct standard keys
    const keys = ['token', 'accessToken', 'jwt', 'auth_token', 'access_token'];
    for (const k of keys) {
        const val = localStorage.getItem(k) || sessionStorage.getItem(k);
        if (val) return val;
    }

    // 2. Check JSON state objects
    try {
        const rawAuth = localStorage.getItem('auth') || localStorage.getItem('user_session') || localStorage.getItem('user');
        if (rawAuth) {
            const parsed = JSON.parse(rawAuth);
            return parsed.token || parsed.accessToken || parsed.jwt || parsed?.user?.token || null;
        }
    } catch {
        // ignore parse error
    }

    return null;
}

export const rawApi = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request Interceptor: Attach Bearer token to EVERY request
api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const rawToken = getStoredToken();
        if (rawToken) {
            // Clean token string in case it already contains quotes or "Bearer "
            const cleanToken = rawToken.replace(/^Bearer\s+/i, '').replace(/^"|"$/g, '');
            config.headers = config.headers || {};
            config.headers.Authorization = `Bearer ${cleanToken}`;
        } else {
            console.warn(`[API] Making request to ${config.url} with NO token present in localStorage!`);
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response Interceptor: Only trigger global logout on specific auth check endpoints, not standard actions
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const url = error.config?.url || '';

        if (error.response?.status === 401) {
            console.error(`[API] 401 Unauthorized received on ${url}`);

            // Only broadcast global unauthorized logout if it's the current user profile check (/users/me or /auth/me)
            // This prevents minor 401s on other endpoints from abruptly kicking the user out to /login
            if (url.includes('/users/me') || url.includes('/auth/me')) {
                if (typeof window !== 'undefined') {
                    window.dispatchEvent(new CustomEvent(AUTH_UNAUTHORIZED_EVENT));
                }
            }
        }

        return Promise.reject(error);
    }
);

export default api;