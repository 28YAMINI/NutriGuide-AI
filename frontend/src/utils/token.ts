/**
 * JWT storage layer.
 *
 * The only place the app touches the token in localStorage — services
 * and contexts use these helpers instead of raw storage calls.
 */
const TOKEN_KEY = 'nutriguide-token'

export function getToken(): string | null {
  return window.localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  window.localStorage.removeItem(TOKEN_KEY)
}

export function hasToken(): boolean {
  return getToken() !== null
}