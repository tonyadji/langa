/**
 * T208: Unit tests for useApplicationUsage hook
 * 
 * Tests the custom hook for fetching application storage usage statistics.
 * Covers initialization, loading states, successful data fetching, and error handling.
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { useApplicationUsage } from './useApplicationUsage';
import { apiClient } from '@/services/api';

// Mock the apiClient
vi.mock('@/services/api', () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

// Mock data in backend format
const mockBackendUsageData = {
  id: 'usage-123',
  key: 'app-abc-123',
  name: 'Test App',
  logUsage: 1024000,
  metricUsage: 512000,
};

// Expected frontend format after mapping
const mockUsageData = {
  id: 'usage-123',
  appKey: 'app-abc-123',
  totalLogBytes: 1024000,
  totalMetricBytes: 512000,
};

describe('useApplicationUsage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should initialize with loading state', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockImplementation(() =>
      new Promise(() => {}) // Never resolves
    );

    const { result } = renderHook(() => useApplicationUsage('app-123'));

    expect(result.current.isLoading).toBe(true);
    expect(result.current.isError).toBe(false);
    expect(result.current.data).toBeUndefined();
    expect(result.current.error).toBeNull();
  });

  it('should fetch and return usage data successfully', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: mockBackendUsageData,
    });

    const { result } = renderHook(() => useApplicationUsage('app-123'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toEqual(mockUsageData);
    expect(result.current.isError).toBe(false);
    expect(result.current.error).toBeNull();
    expect(apiClient.get).toHaveBeenCalledWith('/applications/app-123/usage');
  });

  it('should handle 403 Forbidden error (non-owner access)', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockRejectedValue({
      response: { status: 403 },
    });

    const { result } = renderHook(() => useApplicationUsage('app-123'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.isError).toBe(true);
    expect(result.current.error?.message).toContain('Forbidden');
    expect(result.current.data).toBeUndefined();
  });

  it('should handle 404 Not Found error', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockRejectedValue({
      response: { status: 404 },
    });

    const { result } = renderHook(() => useApplicationUsage('app-999'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.isError).toBe(true);
    expect(result.current.error?.message).toContain('not found');
    expect(result.current.data).toBeUndefined();
  });

  it('should handle network errors', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockRejectedValue(
      new Error('Network error')
    );

    const { result } = renderHook(() => useApplicationUsage('app-123'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.isError).toBe(true);
    expect(result.current.error?.message).toBe('Network error');
    expect(result.current.data).toBeUndefined();
  });

  it('should not fetch if appId is empty', () => {
    const { result } = renderHook(() => useApplicationUsage(''));

    expect(result.current.isLoading).toBe(false);
    expect(result.current.data).toBeUndefined();
    expect(apiClient.get).not.toHaveBeenCalled();
  });

  it('should refetch when appId changes', async () => {
    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: mockBackendUsageData,
    });

    const { result, rerender } = renderHook(
      ({ appId }) => useApplicationUsage(appId),
      {
        initialProps: { appId: 'app-123' },
      }
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(apiClient.get).toHaveBeenCalledTimes(1);
    expect(apiClient.get).toHaveBeenCalledWith('/applications/app-123/usage');

    // Change appId
    rerender({ appId: 'app-456' });

    await waitFor(() => {
      expect(apiClient.get).toHaveBeenCalledTimes(2);
    });

    expect(apiClient.get).toHaveBeenCalledWith('/applications/app-456/usage');
  });

  it('should handle zero bytes edge case', async () => {
    const zeroBackendData = {
      id: 'usage-zero',
      key: 'app-xyz',
      name: 'Zero App',
      logUsage: 0,
      metricUsage: 0,
    };

    const zeroUsageData = {
      id: 'usage-zero',
      appKey: 'app-xyz',
      totalLogBytes: 0,
      totalMetricBytes: 0,
    };

    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: zeroBackendData,
    });

    const { result } = renderHook(() => useApplicationUsage('app-xyz'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toEqual(zeroUsageData);
    expect(result.current.data?.totalLogBytes).toBe(0);
    expect(result.current.data?.totalMetricBytes).toBe(0);
  });

  it('should handle large byte values (terabytes)', async () => {
    const largeBackendData = {
      id: 'usage-large',
      key: 'app-large',
      name: 'Large App',
      logUsage: 1099511627776, // 1 TB
      metricUsage: 549755813888, // 512 GB
    };

    const largeUsageData = {
      id: 'usage-large',
      appKey: 'app-large',
      totalLogBytes: 1099511627776, // 1 TB
      totalMetricBytes: 549755813888, // 512 GB
    };

    (apiClient.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: largeBackendData,
    });

    const { result } = renderHook(() => useApplicationUsage('app-large'));

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.data).toEqual(largeUsageData);
    expect(result.current.data?.totalLogBytes).toBe(1099511627776);
    expect(result.current.data?.totalMetricBytes).toBe(549755813888);
  });
});
