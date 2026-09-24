/**
 * Unit Test: useApplications hook
 * 
 * Tests the useApplications custom hook for fetching and managing application list state.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { useApplications } from '@/features/applications/hooks/useApplications';
import type { Application } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

const mockApplications: Application[] = [
  {
    id: 'app-1',
    name: 'App 1',
    key: 'app-1-key',
    accountKey: 'account-key',
    ingestionUri: 'http://localhost:8080/ingest/app-1-key',
    owner: 'user@example.com',
    sharedWith: [],
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'app-2',
    name: 'App 2',
    key: 'app-2-key',
    accountKey: 'account-key',
    ingestionUri: 'http://localhost:8080/ingest/app-2-key',
    owner: 'user@example.com',
    sharedWith: [],
    createdAt: '2024-01-02T00:00:00Z',
  },
];

const handlers = [
  http.get(`${API_BASE_URL}/applications`, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '1');
    const limit = parseInt(url.searchParams.get('limit') || '10');
    
    return HttpResponse.json({
      applications: mockApplications,
      total: mockApplications.length,
      page,
      limit,
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

describe('useApplications', () => {
  it('should return applications data', async () => {
    const { result } = renderHook(() => useApplications());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.applications).toEqual(mockApplications);
  });
  
  it('should set loading state while fetching', () => {
    const { result } = renderHook(() => useApplications());
    
    expect(result.current.isLoading).toBe(true);
  });
  
  it('should set error state on fetch failure', async () => {
    server.use(
      http.get(`${API_BASE_URL}/applications`, () => {
        return HttpResponse.json(
          { error: 'Failed to fetch' },
          { status: 500 }
        );
      })
    );
    
    const { result } = renderHook(() => useApplications());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.error).toBeDefined();
  });
  
  it('should provide refetch function', async () => {
    const { result } = renderHook(() => useApplications());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(typeof result.current.refetch).toBe('function');
  });
  
  it('should support pagination', async () => {
    const { result } = renderHook(() => 
      useApplications({ page: 2, limit: 5 })
    );
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.pagination).toEqual({
      page: 2,
      limit: 5,
      total: mockApplications.length,
    });
  });
  
  it('should return empty array when no applications', async () => {
    server.use(
      http.get(`${API_BASE_URL}/applications`, () => {
        return HttpResponse.json({
          applications: [],
          total: 0,
          page: 1,
          limit: 10,
        });
      })
    );
    
    const { result } = renderHook(() => useApplications());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    expect(result.current.applications).toEqual([]);
  });
  
  it('should cache results', async () => {
    const fetchSpy = vi.fn();
    server.use(
      http.get(`${API_BASE_URL}/applications`, () => {
        fetchSpy();
        return HttpResponse.json({
          applications: mockApplications,
          total: mockApplications.length,
          page: 1,
          limit: 10,
        });
      })
    );
    
    const { result, rerender } = renderHook(() => useApplications());
    
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });
    
    rerender();
    
    // Should not fetch again on rerender
    expect(fetchSpy).toHaveBeenCalledTimes(1);
  });
});
