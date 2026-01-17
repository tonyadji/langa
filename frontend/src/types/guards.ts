// Type guards for runtime type checking

import type { ApiError } from './api';

export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'message' in error &&
    'code' in error &&
    typeof (error as ApiError).message === 'string' &&
    typeof (error as ApiError).code === 'string'
  );
}

export function isPaginatedResponse<T>(
  response: unknown
): response is { data: T[]; total: number; page: number; limit: number } {
  return (
    typeof response === 'object' &&
    response !== null &&
    'data' in response &&
    'total' in response &&
    'page' in response &&
    'limit' in response &&
    Array.isArray((response as { data: unknown }).data) &&
    typeof (response as { total: unknown }).total === 'number' &&
    typeof (response as { page: unknown }).page === 'number' &&
    typeof (response as { limit: unknown }).limit === 'number'
  );
}
