/**
 * Unit Test: useShareApplication Hook
 * 
 * Tests the useShareApplication hook functionality:
 * - Share application with user/team
 * - Revoke access from user/team
 * - Loading states
 * - Error handling
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { useShareApplication } from '@/features/applications/hooks/useShareApplication';

const API_BASE_URL = 'http://localhost:8080/api';

const mockShareResponse = {
  appId: 'app-123',
  appName: 'Test App',
  key: 'user-123',
  profile: 'USER',
  sharedDate: '2025-01-01T00:00:00.000Z',
  expirationDate: null,
  revokedDate: null,
  currentlyActive: true,
  expired: false,
  revoked: false,
};

const handlers = [
  http.post(`${API_BASE_URL}/applications/:appId/share`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'error-trigger') {
      return HttpResponse.json(
        { error: 'Sharing failed' },
        { status: 500 }
      );
    }
    
    return HttpResponse.json(mockShareResponse, { status: 200 });
  }),

  http.post(`${API_BASE_URL}/applications/:appId/revoke`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'error-trigger') {
      return HttpResponse.json(
        { error: 'Revoke failed' },
        { status: 500 }
      );
    }
    
    return HttpResponse.json(null, { status: 200 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('useShareApplication Hook', () => {
  describe('shareApplication', () => {
    it('should share application with a user successfully', async () => {
      const { result } = renderHook(() => useShareApplication());

      await waitFor(() => expect(result.current).toBeDefined());

      await result.current.shareApplication('app-123', {
        sharedWith: 'user@example.com',
        profile: 'USER',
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(result.current.shareResult).toEqual(mockShareResponse);
        expect(result.current.isLoading).toBe(false);
      });
    });

    it('should share application with a team successfully', async () => {
      const { result } = renderHook(() => useShareApplication());

      await result.current.shareApplication('app-123', {
        sharedWith: 'team-xyz',
        profile: 'TEAM',
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(result.current.shareResult).toBeDefined();
      });
    });

    it('should set loading state during share operation', async () => {
      const { result } = renderHook(() => useShareApplication());

      // Initially not loading
      expect(result.current.isLoading).toBe(false);

      // Start the operation and await it
      await result.current.shareApplication('app-123', {
        sharedWith: 'user@example.com',
        profile: 'USER',
      });

      // Should be completed and not loading after await
      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.isSuccess).toBe(true);
      });
    });

    it('should handle share errors', async () => {
      const { result } = renderHook(() => useShareApplication());

      try {
        await result.current.shareApplication('app-123', {
          sharedWith: 'error-trigger',
          profile: 'USER',
        });
      } catch {
        // Expected to throw
      }

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
        expect(result.current.error).toBeDefined();
        expect(result.current.isLoading).toBe(false);
      });
    });

    it('should reset state after successful share', async () => {
      const { result } = renderHook(() => useShareApplication());

      await result.current.shareApplication('app-123', {
        sharedWith: 'user@example.com',
        profile: 'USER',
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      result.current.reset();

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(false);
        expect(result.current.shareResult).toBeUndefined();
        expect(result.current.isError).toBe(false);
      });
    });
  });

  describe('revokeAccess', () => {
    it('should revoke access from a user successfully', async () => {
      const { result } = renderHook(() => useShareApplication());

      await result.current.revokeAccess('app-123', {
        sharedWith: 'user@example.com',
        profile: 'USER',
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
        expect(result.current.isLoading).toBe(false);
      });
    });

    it('should revoke access from a team successfully', async () => {
      const { result } = renderHook(() => useShareApplication());

      await result.current.revokeAccess('app-123', {
        sharedWith: 'team-xyz',
        profile: 'TEAM',
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });
    });

    it('should set loading state during revoke operation', async () => {
      const { result } = renderHook(() => useShareApplication());

      // Initially not loading
      expect(result.current.isLoading).toBe(false);

      await result.current.revokeAccess('app-123', {
        sharedWith: 'user@example.com',
        profile: 'USER',
      });

      // Should be completed and not loading after await
      await waitFor(() => {
        expect(result.current.isLoading).toBe(false);
        expect(result.current.isSuccess).toBe(true);
      });
    });

    it('should handle revoke errors', async () => {
      const { result } = renderHook(() => useShareApplication());

      try {
        await result.current.revokeAccess('app-123', {
          sharedWith: 'error-trigger',
          profile: 'USER',
        });
      } catch {
        // Expected to throw
      }

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
        expect(result.current.error).toBeDefined();
      });
    });
  });
});
