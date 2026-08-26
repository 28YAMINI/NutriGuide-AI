/**
 * Extracts a user-safe message from any thrown value.
 *
 * Handles Error instances, raw strings, and everything else with a
 * generic fallback — every code path returns a string.
 */
export function getErrorMessage(error: unknown): string {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'string' && error.trim() !== '') {
    return error
  }
  return 'Something went wrong. Please try again.'
}