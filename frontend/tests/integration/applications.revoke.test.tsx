/**
 * Contract Test: POST /api/applications/{id}/revoke
 * 
 * Verifies the revoke access API endpoint behavior as documented in the backend specification.
 * These tests validate request/response contracts and error scenarios.
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationApi } from '@/services/applicationApi';

const API_BASE_URL = 'http://localhost:8080/api';

const handlers = [
  // Successful revoke for user
  http.post(`${API_BASE_URL}/applications/app-1/revoke`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'user@example.com' && body.profile === 'USER') {
      return HttpResponse.json(null, { status: 200 });
    }
    
    return HttpResponse.json(
      { error: 'Invalid request' },
      { status: 400 }
    );
  }),

  // Successful revoke for team
  http.post(`${API_BASE_URL}/applications/app-2/revoke`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    if (body.sharedWith === 'team-xyz' && body.profile === 'TEAM') {
      return HttpResponse.json(null, { status: 200 });
    }
    
    return HttpResponse.json(
      { error: 'Invalid request' },
      { status: 400 }
    );
  }),

  // Non-owner attempts to revoke (403 Forbidden)
  http.post(`${API_BASE_URL}/applications/app-owned-by-other/revoke`, () => {
    return HttpResponse.json(
      { error: 'Only application owners can revoke access' },
      { status: 403 }
    );
  }),

  // Application not found (404)
  http.post(`${API_BASE_URL}/applications/non-existent-app/revoke`, () => {
    return HttpResponse.json(
      { error: 'Application not found' },
      { status: 404 }
    );
  }),

  // User not shared with application (404)
  http.post(`${API_BASE_URL}/applications/app-not-shared/revoke`, () => {
    return HttpResponse.json(
      { error: 'User does not have access to this application' },
      { status: 404 }
    );
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/applications/{id}/revoke - Contract Tests', () => {
  describe('Successful Revocation', () => {
    it('should revoke access from a user by email', async () => {
      await expect(
        applicationApi.revokeAccess('app-1', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).resolves.toBeUndefined();
    });

    it('should revoke access from a team by team key', async () => {
      await expect(
        applicationApi.revokeAccess('app-2', {
          sharedWith: 'team-xyz',
          profile: 'TEAM',
        })
      ).resolves.toBeUndefined();
    });
  });

  describe('Error Scenarios', () => {
    it('should return 403 when non-owner attempts to revoke', async () => {
      await expect(
        applicationApi.revokeAccess('app-owned-by-other', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });

    it('should return 404 when application does not exist', async () => {
      await expect(
        applicationApi.revokeAccess('non-existent-app', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });

    it('should return 404 when user does not have access', async () => {
      await expect(
        applicationApi.revokeAccess('app-not-shared', {
          sharedWith: 'user@example.com',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });

    it('should return 400 for invalid revoke request', async () => {
      await expect(
        applicationApi.revokeAccess('app-1', {
          sharedWith: 'invalid-email',
          profile: 'USER',
        })
      ).rejects.toThrow();
    });
  });

  describe('Request Validation', () => {
    it('should send correct request body for user revocation', async () => {
      await applicationApi.revokeAccess('app-1', {
        sharedWith: 'user@example.com',
        profile: 'USER',
      });
      // No error means request was successful
      expect(true).toBe(true);
    });

    it('should send correct request body for team revocation', async () => {
      await applicationApi.revokeAccess('app-2', {
        sharedWith: 'team-xyz',
        profile: 'TEAM',
      });
      // No error means request was successful
      expect(true).toBe(true);
    });
  });
});
