/**
 * Token storage layer.
 *
 * The only place the app touches tokens in localStorage — services and
 * contexts use these helpers instead of raw storage calls.
 *
 * - Access token: short-lived JWT attached to every request.
 * - Refresh token: long-lived opaque token used only to mint a new pair.
 */
const TOKEN_KEY = 'nutriguide-token'
const REFRESH_TOKEN_KEY = 'nutriguide-refresh-token'

export function getToken(): string | null {
  return window.localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function getRefreshToken(): string | null {
  return window.localStorage.getItem(REFRESH_TOKEN_KEY)
}

export function setRefreshToken(token: string): void {
  window.localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/** Removes the access token only (kept for callers that don't know about refresh). */
export function clearToken(): void {
  window.localStorage.removeItem(TOKEN_KEY)
}

/** Removes both tokens — used on logout and failed refresh. */
export function clearTokens(): void {
  window.localStorage.removeItem(TOKEN_KEY)
  window.localStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function hasToken(): boolean {
  return getToken() !== null
}