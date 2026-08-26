import api, { rawApi } from './api'

import type {
  LoginRequest,
  LoginResponse,
  LogoutRequest,
  RefreshRequest,
  RefreshResponse,
  RegisterRequest,
  RegisterResponse,
} from '../types/auth'

/** Authentication API calls — thin wrappers over the shared axios instance. */
export const authService = {
  async register(payload: RegisterRequest): Promise<RegisterResponse> {
    const { data } = await api.post<RegisterResponse>('/auth/register', payload)
    return data
  },

  async login(payload: LoginRequest): Promise<LoginResponse> {
    const { data } = await api.post<LoginResponse>('/auth/login', payload)
    return data
  },

  /**
   * Mints a new token pair. Runs on the bare instance (no interceptors)
   * so a failed refresh can never loop back into itself.
   */
  async refresh(payload: RefreshRequest): Promise<RefreshResponse> {
    const { data } = await rawApi.post<RefreshResponse>('/auth/refresh', payload)
    return data
  },

  /** Revokes the presented refresh token server-side. */
  async logout(payload: LogoutRequest): Promise<void> {
    await rawApi.post('/auth/logout', payload)
  },
}