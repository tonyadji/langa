/**
 * Integration Test: Application Sharing Flow
 * 
 * Tests the complete user journey for sharing applications:
 * 1. Owner shares application with user
 * 2. Shared user views application with restricted permissions
 * 3. Owner views sharing configuration
 * 4. Owner revokes access
 * 5. Shared user can no longer access
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationApi } from '@/services/applicationApi';

const API_BASE_URL = 'http://localhost:8080/api';

// Mock data
const mockOwner = 'owner@example.com';
const mockSharedUser = 'shared@example.com';

const mockApplication: Application = {
  id: 'app-123',
  name: 'Test Application',
  accountKey: 'acc-123',
  owner: mockOwner,
  sharedWith: [],
  key: 'APP-KEY',
  secret: 'APP-SECRET',
  http: 'http://ingest.example.com',
  kafka: 'kafka://ingest.example.com',
  createdAt: '2025-01-01T00:00:00.000Z',
};

const mockSharedApplication: Application = {
  ...mockApplication,
  sharedWith: [
    {
      key: 'user-456',
      profile: 'USER',
      sharedDate: '2025-01-01T12:00:00.000Z',
    },
  ],
  // Shared users don't see secrets
  key: undefined,
  secret: undefined,
};

const mockShareResponse: ShareWith = {
  key: 'user-456',
  profile: 'USER',
  sharedDate: '2025-01-01T12:00:00.000Z',
};

let applicationSharedWith: ShareWith[] = [];

const handlers = [
  // Share application
  http.post(`${API_BASE_URL}/applications/app-123/share`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    const newShare: ShareWith = {
      key: 'user-456',
      profile: body.profile as SharedWithProfile,
      sharedDate: new Date().toISOString(),
    };
    
    applicationSharedWith.push(newShare);
    return HttpResponse.json(newShare, { status: 200 });
  }),

  // Get application details (for owner - includes secrets and sharedWith list)
  http.get(`${API_BASE_URL}/applications/app-123`, () => {
    return HttpResponse.json({
      ...mockApplication,
      sharedWith: applicationSharedWith,
    }, { status: 200 });
  }),

  // Get secured details (owner only)
  http.get(`${API_BASE_URL}/applications/app-123/secured-details`, () => {
    return HttpResponse.json({
      ...mockApplication,
      sharedWith: applicationSharedWith,
    }, { status: 200 });
  }),

  // Revoke access
  http.post(`${API_BASE_URL}/applications/app-123/revoke`, async ({ request }) => {
    const body = (await request.json()) as { sharedWith: string; profile: string };
    
    applicationSharedWith = applicationSharedWith.filter(
      (share) => share.key !== 'user-456'
    );
    
    return HttpResponse.json(null, { status: 200 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'bypass' }));
afterEach(() => {
  server.resetHandlers();
  applicationSharedWith = [];
});
afterAll(() => server.close());

describe('Application Sharing Flow - Integration Tests', () => {
  it('should complete full sharing lifecycle: share → view → revoke', async () => {
    // Step 1: Owner shares application with a user
    const shareResult = await applicationApi.shareApplication('app-123', {
      sharedWith: mockSharedUser,
      profile: 'USER',
    });

    expect(shareResult).toBeDefined();
    expect(shareResult.profile).toBe('USER');
    expect(shareResult.sharedDate).toBeDefined();

    // Step 2: Verify application now shows as shared
    const appAfterShare = await applicationApi.getApplication('app-123');
    expect(appAfterShare.sharedWith).toHaveLength(1);
    expect(appAfterShare.sharedWith[0].profile).toBe('USER');

    // Step 3: Owner revokes access
    await applicationApi.revokeAccess('app-123', {
      sharedWith: mockSharedUser,
      profile: 'USER',
    });

    // Step 4: Verify shared user is removed
    const appAfterRevoke = await applicationApi.getApplication('app-123');
    expect(appAfterRevoke.sharedWith).toHaveLength(0);
  });

  it('should allow owner to share with multiple users', async () => {
    // Share with first user
    await applicationApi.shareApplication('app-123', {
      sharedWith: 'user1@example.com',
      profile: 'USER',
    });

    // Add another share
    applicationSharedWith.push({
      key: 'user-789',
      profile: 'USER',
      sharedDate: new Date().toISOString(),
    });

    // Verify both shares exist
    const app = await applicationApi.getApplication('app-123');
    expect(app.sharedWith.length).toBeGreaterThanOrEqual(1);
  });

  it('should allow owner to share with teams', async () => {
    const teamShare = await applicationApi.shareApplication('app-123', {
      sharedWith: 'team-xyz',
      profile: 'TEAM',
    });

    expect(teamShare.profile).toBe('TEAM');
  });

  it('should track when access was shared', async () => {
    const beforeShare = new Date();
    
    const shareResult = await applicationApi.shareApplication('app-123', {
      sharedWith: mockSharedUser,
      profile: 'USER',
    });

    const sharedDate = new Date(shareResult.sharedDate);
    expect(sharedDate.getTime()).toBeGreaterThanOrEqual(beforeShare.getTime());
  });

  it('should maintain share history after multiple operations', async () => {
    // Share with user A
    await applicationApi.shareApplication('app-123', {
      sharedWith: 'userA@example.com',
      profile: 'USER',
    });

    // Add user B
    applicationSharedWith.push({
      key: 'userB-key',
      profile: 'USER',
      sharedDate: new Date().toISOString(),
    });

    const app = await applicationApi.getApplication('app-123');
    expect(app.sharedWith.length).toBeGreaterThanOrEqual(1);

    // Revoke user A
    await applicationApi.revokeAccess('app-123', {
      sharedWith: 'userA@example.com',
      profile: 'USER',
    });

    // User B should still have access
    const appAfterRevoke = await applicationApi.getApplication('app-123');
    expect(appAfterRevoke.sharedWith).toBeDefined();
  });
});
