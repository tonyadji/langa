/**
 * Contract Test: POST /api/applications/{id}/share
 * 
 * Verifies the share API endpoint behavior as documented in the backend specification.
 * These tests validate request/response contracts and error scenarios.
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationApi } from '@/services/applicationApi';

const API_BASE_URL = 'http://localhost:8080/api';

const mockShareWith = {
  appId: 'app-1',
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
  // Successful share with user
  http.post(`${API_BASE_URL}/applications/app-1/share`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'shared-user@example.com') {
      return HttpResponse.json(mockShareWith, { status: 200 });
    }
    
    return HttpResponse.json(
      { error: 'Invalid request' },
      { status: 400 }
    );
  }),

  // Successful share with team
  http.post(`${API_BASE_URL}/applications/app-2/share`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'team@example.com' && body.profile === 'TEAM') {
      return HttpResponse.json({
        appId: 'app-2',
        appName: 'Test App 2',
        key: 'team-xyz',
        profile: 'TEAM',
        sharedDate: '2025-01-01T00:00:00.000Z',
        expirationDate: null,
        revokedDate: null,
        currentlyActive: true,
        expired: false,
        revoked: false,
      }, { status: 200 });
    }
    
    return HttpResponse.json(
      { error: 'Invalid request' },
      { status: 400 }
    );
  }),

  // Non-owner attempts to share (403 Forbidden)
  http.post(`${API_BASE_URL}/applications/app-owned-by-other/share`, () => {
    return HttpResponse.json(
      { error: 'Only application owners can share applications' },
      { status: 403 }
    );
  }),

  // Application not found (404)
  http.post(`${API_BASE_URL}/applications/non-existent-app/share`, () => {
    return HttpResponse.json(
      { error: 'Application not found' },
      { status: 404 }
    );
  }),

  // Already shared (409 Conflict)
  http.post(`${API_BASE_URL}/applications/app-already-shared/share`, () => {
    return HttpResponse.json(
      { error: 'Application already shared with this user' },
      { status: 409 }
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/applications/{id}/share - Contract Tests', () => {
  describe('Successful Sharing', () => {
    it('should share application with a user by email', async () => {
      const result = await applicationApi.shareApplication('app-1', {
        sharedWith: 'shared-user@example.com',
        profile: 'USER',
      });

      expect(result).toEqual(mockShareWith);
      expect(result.key).toBe('user-123');
      expect(result.profile).toBe('USER');
      expect(result.sharedDate).toBe('2025-01-01T00:00:00.000Z');
    });

    it('should share application with a team by team key', async () => {
      const result = await applicationApi.shareApplication('app-2', {
        sharedWith: 'team@example.com',
        profile: 'TEAM',
      });

      expect(result).toBeDefined();
      expect(result.key).toBe('team-xyz');
      expect(result.profile).toBe('TEAM');
      expect(result.sharedDate).toBeDefined();
    });
  });

  describe('Error Scenarios', () => {
    it('should return 403 when non-owner attempts to share', async () => {
      await expect(
        applicationApi.shareApplication('app-owned-by-other', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });

    it('should return 404 when application does not exist', async () => {
      await expect(
        applicationApi.shareApplication('non-existent-app', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });

    it('should return 409 when application already shared with user', async () => {
      await expect(
        applicationApi.shareApplication('app-already-shared', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });

    it('should return 400 for invalid share request', async () => {
      await expect(
        applicationApi.shareApplication('app-1', {
          sharedWith: 'invalid-email',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });
  });

  describe('Request Validation', () => {
    it('should send correct request body for user sharing', async () => {
      const result = await applicationApi.shareApplication('app-1', {
        sharedWith: 'shared-user@example.com',
        profile: 'USER',
      });

      expect(result).toBeDefined();
    });

    it('should send correct request body for team sharing', async () => {
      const result = await applicationApi.shareApplication('app-2', {
        sharedWith: 'team@example.com',
        profile: 'TEAM',
      });

      expect(result.profile).toBe('TEAM');
    });
  });
});
