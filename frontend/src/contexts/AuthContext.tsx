import { createContext, useCallback, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

import api, { AUTH_UNAUTHORIZED_EVENT } from '../services/api'
import { authService } from '../services/authService'
import { clearToken, hasToken, setToken } from '../utils/token'

import type { LoginRequest, RegisterRequest } from '../types/auth'
import type { UserResponse } from '../types/user'

export interface AuthContextValue {
  user: UserResponse | null
  isAuthenticated: boolean
  isAdmin: boolean
  isLoading: boolean
  login: (payload: LoginRequest) => Promise<void>
  register: (payload: RegisterRequest) => Promise<void>
  logout: () => void
  setUser: (user: UserResponse | null) => void
}

interface AuthProviderProps {
  children: ReactNode
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUserState] = useState<UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const login = useCallback(async (payload: LoginRequest) => {
    const response = await authService.login(payload)
    setToken(response.token)
    setUserState(response.user)
  }, [])

  const register = useCallback(async (payload: RegisterRequest) => {
    // Backend returns no token on registration — the user signs in next.
    await authService.register(payload)
  }, [])

  const logout = useCallback(() => {
    clearToken()
    setUserState(null)
  }, [])

  const setUser = useCallback((nextUser: UserResponse | null) => {
    setUserState(nextUser)
  }, [])

  // Session restore: a stored token means the user may still be signed
  // in — verify against the backend once on boot.
  useEffect(() => {
    let cancelled = false

    if (!hasToken()) {
      setIsLoading(false)
      return
    }

    api
      .get<UserResponse>('/users/me')
      .then(({ data }) => {
        if (!cancelled) setUserState(data)
      })
      .catch(() => {
        if (!cancelled) {
          clearToken()
          setUserState(null)
        }
      })
      .finally(() => {
        if (!cancelled) setIsLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  // 401 on any protected endpoint (expired/invalid token) → end the session.
  useEffect(() => {
    const handleUnauthorized = () => {
      clearToken()
      setUserState(null)
    }

    window.addEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
    return () => window.removeEventListener(AUTH_UNAUTHORIZED_EVENT, handleUnauthorized)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      isAdmin: user?.role === 'ADMIN',
      isLoading,
      login,
      register,
      logout,
      setUser,
    }),
    [user, isLoading, login, register, logout, setUser],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}