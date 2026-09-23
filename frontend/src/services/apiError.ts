import { isAxiosError } from 'axios';

/** Error body returned by the backend (ApiError). */
interface ApiErrorBody {
  code?: string;
  message?: string;
  details?: string;
  /** Legacy error format */
  error?: string;
}

/** Backend code of validation errors: their details say which field is invalid. */
const VALIDATION_ERROR_CODE = '400';

/**
 * Human readable message of an API call failure: the backend error message (with the details of
 * validation errors), otherwise the error message, otherwise the given fallback.
 */
export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError<ApiErrorBody>(error)) {
    const body = error.response?.data;
    if (body && typeof body === 'object') {
      if (body.message) {
        return body.code === VALIDATION_ERROR_CODE && body.details
          ? `${body.message}: ${body.details}`
          : body.message;
      }
      if (body.error) {
        return body.error;
      }
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  if (typeof error === 'string' && error) {
    return error;
  }
  return fallback;
}
