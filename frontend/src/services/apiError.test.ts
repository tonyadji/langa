import { describe, it, expect } from 'vitest';
import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios';
import { getApiErrorMessage } from './apiError';

const axiosError = (status: number, data: unknown) =>
  new AxiosError(`Request failed with status code ${status}`, 'ERR_BAD_REQUEST', undefined, undefined, {
    status,
    data,
    statusText: '',
    headers: {},
    config: { headers: new AxiosHeaders() },
  } as AxiosResponse);

describe('getApiErrorMessage', () => {
  it('should use the backend error message', () => {
    const error = axiosError(400, { code: '400-002', message: 'Application already shared', details: 'x' });

    expect(getApiErrorMessage(error, 'fallback')).toBe('Application already shared');
  });

  it('should add the details of validation errors', () => {
    const error = axiosError(400, { code: '400', message: 'Validation error', details: 'shareWith: must not be blank' });

    expect(getApiErrorMessage(error, 'fallback')).toBe('Validation error: shareWith: must not be blank');
  });

  it('should still read the legacy error field', () => {
    expect(getApiErrorMessage(axiosError(409, { error: 'Conflict' }), 'fallback')).toBe('Conflict');
  });

  it('should fall back to the error message, then to the given fallback', () => {
    expect(getApiErrorMessage(axiosError(500, ''), 'fallback')).toBe('Request failed with status code 500');
    expect(getApiErrorMessage(new Error('boom'), 'fallback')).toBe('boom');
    expect(getApiErrorMessage(undefined, 'fallback')).toBe('fallback');
  });
});
