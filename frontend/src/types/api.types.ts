/**
 * Standard error response returned by the backend's GlobalExceptionHandler.
 *
 * Every failed API call resolves to this shape, so error handling can
 * rely on a stable, typed contract instead of guessing at response
 * structures.
 */
export interface ApiErrorResponse {
  /** When the error occurred, ISO-8601. */
  timestamp: string
  /** HTTP status code, e.g. 400, 401, 404, 409, 500. */
  status: number
  /** Short HTTP reason, e.g. "Bad Request". */
  error: string
  /** Human-readable message describing the failure. */
  message: string
  /** Request path that produced the error, e.g. "/api/auth/login". */
  path: string
}