/**
 * Unit Test: useMetrics Hook
 * Test ID: T138 | US4 - Metrics Visualization
 * 
 * Tests the useMetrics custom hook for fetching and managing metrics data with filters.
 * 
 * Test Coverage:
 * - Fetches metrics for an application
 * - Handles loading states
 * - Handles errors
 * - Applies filters (name, status, duration, dates)
 * - Refetch functionality
 * - Enabled/disabled state
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { useMetrics } from './useMetrics';
import type { MetricsResponse } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

const mockMetricsResponse: MetricsResponse = {
  appName: 'Test Application',
  paginatedMetrics: {
    content: [
      {
        name: 'api.request',
        durationMillis: 150,
        status: 'SUCCESS',
        timestamp: 1704729600000,
        uri: '/api/users',
        httpMethod: 'GET',
        httpStatus: 200,
      },
      {
        name: 'api.request',
        durationMillis: 250,
        status: 'SUCCESS',
        timestamp: 1704729660000,
        uri: '/api/products',
        httpMethod: 'POST',
        httpStatus: 201,
      },
      {
        name: 'api.request',
        durationMillis: 500,
        status: 'FAILURE',
        timestamp: 1704729720000,
        uri: '/api/orders',
        httpMethod: 'GET',
        httpStatus: 500,
      },
    ],
    totalElements: 3,
    totalPages: 1,
    page: 0,
    size: 20,
  },
};

const handlers = [
  http.get(`${API_BASE_URL}/applications/:appId/metrics`, ({ params, request }) => {
    const url = new URL(request.url);
    const name = url.searchParams.get('name');
    const status = url.searchParams.get('status');
    
    // Filter based on query params
    let filteredContent = [...mockMetricsResponse.paginatedMetrics.content];
    
    if (name) {
      filteredContent = filteredContent.filter(m => m.name.includes(name));
    }
    
    if (status) {
      filteredContent = filteredContent.filter(m => m.status === status);
    }
    
    return HttpResponse.json({
      appName: mockMetricsResponse.appName,
      paginatedMetrics: {
        ...mockMetricsResponse.paginatedMetrics,
        content: filteredContent,
        totalElements: filteredContent.length,
      },
    });
  }),
];

const server = setupServer(...handlers);

beforeEach(() => {
  server.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  server.resetHandlers();
  server.close();
});

describe('useMetrics Hook - Unit Tests', () => {
  it('should fetch metrics for an application', async () => {
    const { result } = renderHook(() =>
      useMetrics({ applicationId: 'test-app-id' })
    );

    // Initially loading
    expect(result.current.isLoading).toBe(true);
    expect(result.current.metrics).toBeNull();

    // Wait for data to load
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.metrics).toEqual(mockMetricsResponse);
    expect(result.current.error).toBeNull();
  });

  it('should handle loading states correctly', async () => {
    const { result } = renderHook(() =>
      useMetrics({ applicationId: 'test-app-id' })
    );

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.metrics).toBeTruthy();
  });

  it('should handle errors', async () => {
    server.use(
      http.get(`${API_BASE_URL}/applications/:appId/metrics`, () => {
        return HttpResponse.json(
          { message: 'Internal Server Error' },
          { status: 500 }
        );
      })
    );

    const { result } = renderHook(() =>
      useMetrics({ applicationId: 'test-app-id' })
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.error).toBeTruthy();
    expect(result.current.metrics).toBeNull();
  });

  it('should filter metrics by name', async () => {
    const { result } = renderHook(() =>
      useMetrics({
        applicationId: 'test-app-id',
        name: 'api.request',
      })
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.metrics?.paginatedMetrics.content).toHaveLength(3);
    expect(
      result.current.metrics?.paginatedMetrics.content.every(m => m.name === 'api.request')
    ).toBe(true);
  });

  it('should filter metrics by status', async () => {
    const { result } = renderHook(() =>
      useMetrics({
        applicationId: 'test-app-id',
        status: 'SUCCESS',
      })
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.metrics?.paginatedMetrics.content).toHaveLength(2);
    expect(
      result.current.metrics?.paginatedMetrics.content.every(m => m.status === 'SUCCESS')
    ).toBe(true);
  });

  it('should support refetch functionality', async () => {
    const { result } = renderHook(() =>
      useMetrics({ applicationId: 'test-app-id' })
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    const firstFetch = result.current.metrics;

    // Trigger refetch
    await result.current.refetch();

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.metrics).toEqual(firstFetch);
  });

  it('should not fetch when enabled is false', async () => {
    const { result } = renderHook(() =>
      useMetrics({
        applicationId: 'test-app-id',
        enabled: false,
      })
    );

    // Should remain in initial state
    expect(result.current.isLoading).toBe(false);
    expect(result.current.metrics).toBeNull();
    expect(result.current.error).toBeNull();
  });

  it('should not fetch when applicationId is missing', async () => {
    const { result } = renderHook(() =>
      useMetrics({
        applicationId: '',
      })
    );

    expect(result.current.isLoading).toBe(false);
    expect(result.current.metrics).toBeNull();
  });

  it('should apply multiple filters simultaneously', async () => {
    const { result } = renderHook(() =>
      useMetrics({
        applicationId: 'test-app-id',
        name: 'api.request',
        status: 'FAILURE',
      })
    );

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.metrics?.paginatedMetrics.content).toHaveLength(1);
    const metric = result.current.metrics?.paginatedMetrics.content[0];
    expect(metric?.name).toBe('api.request');
    expect(metric?.status).toBe('FAILURE');
  });
});
