import api from './api'

import type {
  LoginRequest,
  LoginResponse,
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
}