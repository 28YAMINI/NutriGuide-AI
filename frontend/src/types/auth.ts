import type {
  ActivityLevel,
  Gender,
  Goal,
  Role,
  UserResponse,
} from './user'

/** Body of POST /api/auth/register. */
export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  age: number
  gender: Gender
  /** Height in centimeters. */
  height: number
  /** Weight in kilograms. */
  weight: number
  activityLevel: ActivityLevel
  goal: Goal
}

/** Body of POST /api/auth/login. */
export interface LoginRequest {
  email: string
  password: string
}

/** Response of POST /api/auth/login. */
export interface LoginResponse {
  readonly token: string
  readonly tokenType: 'Bearer'
  readonly user: UserResponse
}

/** Response of POST /api/auth/register. */
export interface RegisterResponse {
  readonly userId: number
  readonly firstName: string
  readonly lastName: string
  readonly email: string
  readonly role: Role
  readonly message: string
}