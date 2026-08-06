/**
 * User domain types — mirrors the backend User module.
 *
 * Enum values match the Java enums exactly (Jackson serializes by name).
 */

export type Role = 'USER' | 'ADMIN'

export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export type ActivityLevel =
  | 'SEDENTARY'
  | 'LIGHT'
  | 'MODERATE'
  | 'ACTIVE'
  | 'VERY_ACTIVE'

export type Goal = 'LOSE_WEIGHT' | 'GAIN_WEIGHT' | 'MAINTAIN_WEIGHT'

/** Response of GET /api/users/me. */
export interface UserResponse {
  readonly userId: number
  readonly firstName: string
  readonly lastName: string
  readonly email: string
  readonly role: Role
  readonly age: number
  readonly gender: Gender
  /** Height in centimeters. */
  readonly height: number
  /** Weight in kilograms. */
  readonly weight: number
  readonly activityLevel: ActivityLevel
  readonly goal: Goal
}

/** Body of PUT /api/users/me — all fields optional (partial update). */
export interface UpdateUserRequest {
  firstName?: string
  lastName?: string
  age?: number
  gender?: Gender
  height?: number
  weight?: number
  activityLevel?: ActivityLevel
  goal?: Goal
}