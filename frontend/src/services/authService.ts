// src/services/authService.ts
import { rawApi } from './api';

export const authService = {
  login: async (credentials: { email: string; password: string }) => {
    // Use rawApi for login to avoid sending stale tokens
    const response = await rawApi.post('/auth/login', credentials);
    if (response.data?.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user || response.data));
    }
    return response.data;
  },

  register: async (data: any) => {
    return rawApi.post('/auth/register', data);
  },

  logout: async (payload?: { refreshToken?: string | null }) => {
    try {
      if (payload?.refreshToken) {
        await rawApi.post('/auth/logout', payload);
      }
    } catch {
      // Best-effort logout: ignore server errors if token is already invalidated
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }
};

export default authService;