import type { UpdateUserRequest, UserResponse } from '../types/user'
import api from './api'


/**
 * User profile module API calls.
 *
 * Both endpoints operate on the authenticated user only.
 * The JWT from the Axios request interceptor identifies the user.
 */
export const userService = {
  /** GET /api/users/me — the authenticated user's profile. */
  getMe(): Promise<UserResponse> {
    return api.get<UserResponse>('/users/me').then((response) => response.data)
  },

  /** PUT /api/users/me — update the authenticated user's own profile. */
  updateMe(payload: UpdateUserRequest): Promise<UserResponse> {
    return api
      .put<UserResponse>('/users/me', payload)
      .then((response) => response.data)
  },
}