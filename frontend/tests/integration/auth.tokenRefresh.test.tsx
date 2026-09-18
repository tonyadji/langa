/**
 * Integration Test: Automatic Token Refresh on 401
 * 
 * Tests that the axios interceptor automatically refreshes expired access tokens
 * when receiving a 401 Unauthorized response, then retries the original request.
 * 
 * ⚠️ This test MUST FAIL until axios interceptor is implemented in api.ts
 */

import { describe, it, expect, beforeAll, afterEach, afterAll, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationsApi } from '@/features/applications/api/applicationsApi';
import { config } from '@/config';

const API_BASE_URL = 'http://localhost:8080/api';

let accessToken = 'expired-access-token';
const refreshToken = 'valid-refresh-token';

const handlers = [
  // Protected endpoint that requires authentication
  http.get(`${API_BASE_URL}/applications`, ({ request }) => {
    const authHeader = request.headers.get('Authorization');
    
    if (!authHeader) {
      return HttpResponse.json(
        { error: 'Missing authorization' },
        { status: 401 }
      );
    }
    
    const token = authHeader.replace('Bearer ', '');
    
    // First call with expired token returns 401
    if (token === 'expired-access-token') {
      return HttpResponse.json(
        { error: 'Token expired' },
        { status: 401 }
      );
    }
    
    // Second call with refreshed token succeeds
    if (token === 'new-access-token') {
      return HttpResponse.json({
        applications: [
          {
            id: 'app-1',
            name: 'Test App',
            key: 'test-app-key',
            accountKey: 'account-123',
            ingestionUri: 'http://localhost:8080/ingest/test-app-key',
            owner: 'user@example.com',
            sharedWith: [],
          },
        ],
        total: 1,
        page: 1,
        limit: 10,
      });
    }
    
    return HttpResponse.json(
      { error: 'Invalid token' },
      { status: 401 }
    );
  }),
  
  // Token refresh endpoint
  http.post(`${API_BASE_URL}/auth/refresh`, async ({ request }) => {
    const body = await request.json() as { refreshToken: string };
    
    if (body.refreshToken !== refreshToken) {
      return HttpResponse.json(
        { error: 'Invalid refresh token' },
        { status: 401 }
      );
    }
    
    // Update access token for subsequent requests
    accessToken = 'new-access-token';
    
    return HttpResponse.json({
      accessToken: 'new-access-token',
      refreshToken: 'new-refresh-token',
      email: 'user@example.com',
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => {
  server.resetHandlers();
  accessToken = 'expired-access-token';
});
afterAll(() => server.close());

describe('Automatic Token Refresh - Integration Test', () => {
  beforeEach(() => {
    // Mock localStorage to provide tokens
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key: string) => {
      if (key === config.tokenKey) return accessToken;
      if (key === config.refreshTokenKey) return refreshToken;
      return null;
    });
    
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation((key: string, value: string) => {
      if (key === config.tokenKey) accessToken = value;
    });
    
    vi.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {});
    vi.spyOn(Storage.prototype, 'clear').mockImplementation(() => {});
  });
  
  it('should automatically refresh token on 401 and retry request', async () => {
    // Make request with expired token
    const result = await applicationsApi.getApplications({});
    
    // Should succeed after automatic token refresh
    expect(result).toBeDefined();
    expect(result.applications).toHaveLength(1);
    expect(result.applications[0].name).toBe('Test App');
  });
  
  it('should update stored access token after refresh', async () => {
    const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
    
    await applicationsApi.getApplications({});
    
    // Verify new token was stored
    expect(setItemSpy).toHaveBeenCalledWith(config.tokenKey, 'new-access-token');
  });
  
  it('should include new access token in retry request', async () => {
    let requestCount = 0;
    
    server.use(
      http.get(`${API_BASE_URL}/applications`, ({ request }) => {
        requestCount++;
        const token = request.headers.get('Authorization')?.replace('Bearer ', '');
        
        if (requestCount === 1) {
          expect(token).toBe('expired-access-token');
          return HttpResponse.json({ error: 'Token expired' }, { status: 401 });
        }
        
        expect(token).toBe('new-access-token');
        return HttpResponse.json([]);
      })
    );
    
    await applicationsApi.getApplications({});
    
    expect(requestCount).toBe(2);
  });
  
  it('should logout user if refresh token is invalid', async () => {
    const removeItemSpy = vi.spyOn(Storage.prototype, 'removeItem');
    
    // Set invalid refresh token
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key: string) => {
      if (key === config.tokenKey) return 'expired-access-token';
      if (key === config.refreshTokenKey) return 'invalid-refresh-token';
      return null;
    });
    
    await expect(applicationsApi.getApplications({})).rejects.toThrow();
    
    // Should clear tokens on failed refresh
    expect(removeItemSpy).toHaveBeenCalledWith(config.tokenKey);
    expect(removeItemSpy).toHaveBeenCalledWith(config.refreshTokenKey);
  });
});
