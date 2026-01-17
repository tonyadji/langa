/**
 * Contract Test: GET /api/applications/{id}
 * 
 * Validates the application details endpoint contract according to the API specification.
 * Tests retrieval of full application details including credentials (owners only).
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationsApi } from '@/features/applications/api/applicationsApi';

const API_BASE_URL = 'http://localhost:8080/api';

const handlers = [
  http.get(`${API_BASE_URL}/applications/:appId`, ({ params }) => {
    const { appId } = params;
    
    if (appId === 'non-existent') {
      return HttpResponse.json(
        { error: 'Application not found' },
        { status: 404 }
      );
    }
    
    if (appId === 'shared-app') {
      return HttpResponse.json(
        { error: 'Unauthorized - only owners can access secured details' },
        { status: 403 }
      );
    }
    
    return HttpResponse.json({
      id: appId,
      name: 'My Application',
      key: 'my-app-key-abc123',
      accountKey: 'account-key-xyz789',
      ingestionUri: 'http://localhost:8080/ingest/my-app-key-abc123',
      owner: 'user@example.com',
      sharedWith: ['team:engineering'],
      createdAt: '2024-01-01T00:00:00Z',
      // Secured fields only returned for owners
      credentials: {
        apiKey: 'secret-api-key-12345',
        apiSecret: 'secret-api-secret-67890',
      },
      ingestionEndpoints: {
        logs: 'http://localhost:8080/ingest/my-app-key-abc123/logs',
        metrics: 'http://localhost:8080/ingest/my-app-key-abc123/metrics',
      },
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('GET /api/applications/{id} - Contract Tests', () => {
  it('should return full application details for owned app', async () => {
    const details = await applicationsApi.getApplicationById('app-1');
    
    expect(details).toBeDefined();
    expect(details.id).toBe('app-1');
    expect(details.name).toBe('My Application');
  });
  
  it('should include credentials for owned applications', async () => {
    const details = await applicationsApi.getApplicationById('app-1');
    
    expect(details.credentials).toBeDefined();
    expect(details.credentials?.apiKey).toBeDefined();
    expect(details.credentials?.apiSecret).toBeDefined();
  });
  
  it('should include ingestion endpoints', async () => {
    const details = await applicationsApi.getApplicationById('app-1');
    
    expect(details.ingestionEndpoints).toBeDefined();
    expect(details.ingestionEndpoints?.logs).toContain('/logs');
    expect(details.ingestionEndpoints?.metrics).toContain('/metrics');
  });
  
  it('should include basic application info', async () => {
    const details = await applicationsApi.getApplicationById('app-1');
    
    expect(details.key).toBeDefined();
    expect(details.accountKey).toBeDefined();
    expect(details.ingestionUri).toBeDefined();
    expect(details.owner).toBe('user@example.com');
    expect(details.sharedWith).toBeInstanceOf(Array);
    expect(details.createdAt).toBeDefined();
  });
  
  it('should return 404 for non-existent application', async () => {
    await expect(
      applicationsApi.getApplicationById('non-existent')
    ).rejects.toThrow();
  });
  
  it('should return 403 for shared applications (non-owners)', async () => {
    await expect(
      applicationsApi.getApplicationById('shared-app')
    ).rejects.toThrow();
  });
  
  it('should include sharedWith list', async () => {
    const details = await applicationsApi.getApplicationById('app-1');
    
    expect(details.sharedWith).toContain('team:engineering');
  });
});
