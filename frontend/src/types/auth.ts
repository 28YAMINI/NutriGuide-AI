// File: src/types/auth.ts

import type {
  ActivityLevel,
  Gender,
  Goal,
  Role,
  UserResponse,
} from './user';

/** Body of POST /api/auth/register */
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  age: number;
  gender: Gender;
  /** Height in centimeters. */
  height: number;
  /** Weight in kilograms. */
  weight: number;
  activityLevel: ActivityLevel;
  goal: Goal;
}

/** Response of POST /api/auth/register */
export interface RegisterResponse {
  readonly userId: number | string;
  readonly firstName: string;
  readonly lastName: string;
  readonly email: string;
  readonly role?: Role;
  readonly message: string;
  readonly token?: string;
  readonly refreshToken?: string;
  readonly tokenType?: 'Bearer';
  readonly user?: UserResponse;
}

/** Body of POST /api/auth/login */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Response of POST /api/auth/login */
export interface LoginResponse {
  readonly token: string;
  readonly tokenType: 'Bearer';
  readonly refreshToken: string;
  readonly user: UserResponse;
  readonly expiresIn?: number;
  readonly message?: string;
}

/** Body of POST /api/auth/refresh */
export interface RefreshRequest {
  refreshToken: string;
}

/** Response of POST /api/auth/refresh */
export interface RefreshResponse {
  readonly token: string;
  readonly refreshToken: string;
  readonly tokenType: 'Bearer';
  readonly expiresIn?: number;
}

/** Body of POST /api/auth/logout */
export interface LogoutRequest {
  refreshToken: string;
}

/** Generic Auth API error response */
export interface AuthErrorResponse {
  readonly message: string;
  readonly statusCode?: number;
  readonly errors?: Record<string, string[]>;
}

/** Client-side Auth State (for Context / Redux / Zustand) */
export interface AuthState {
  user: UserResponse | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

/** Decoded JWT token payload */
export interface DecodedTokenPayload {
  sub: string;
  email: string;
  role: Role;
  iat: number;
  exp: number;
}