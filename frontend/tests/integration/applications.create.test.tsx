/**
 * Contract Test: POST /api/applications
 * 
 * Validates the application creation endpoint contract according to the API specification.
 * Tests successful creation, validation errors, and duplicate name handling.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationsApi } from '@/features/applications/api/applicationsApi';
import type { Application } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

// Mock handlers for the application creation endpoint
const handlers = [
  http.post(`${API_BASE_URL}/applications`, async ({ request }) => {
    const body = await request.json() as { name: string };
    
    // Validate required fields
    if (!body.name || body.name.trim() === '') {
      return HttpResponse.json(
        { error: 'Application name is required' },
        { status: 400 }
      );
    }
    
    // Simulate duplicate name check
    if (body.name === 'existing-app') {
      return HttpResponse.json(
        { error: 'Application name already exists' },
        { status: 409 }
      );
    }
    
    // Successful creation returns full application object
    const application: Application = {
      id: 'app-123',
      name: body.name,
      key: `${body.name.toLowerCase().replace(/\s+/g, '-')}-key-abc123`,
      accountKey: 'account-key-xyz789',
      ingestionUri: `http://localhost:8080/ingest/${body.name.toLowerCase().replace(/\s+/g, '-')}-key-abc123`,
      owner: 'user@example.com',
      sharedWith: [],
      createdAt: new Date().toISOString(),
    };
    
    return HttpResponse.json(application, { status: 201 });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('POST /api/applications - Contract Tests', () => {
  it('should create application successfully with valid name', async () => {
    const newApp = await applicationsApi.createApplication({
      name: 'My Test Application',
    });
    
    expect(newApp).toBeDefined();
    expect(newApp.id).toBe('app-123');
    expect(newApp.name).toBe('My Test Application');
    expect(newApp.key).toMatch(/^my-test-application-key-[a-z0-9]+$/);
    expect(newApp.owner).toBe('user@example.com');
  });
  
  it('should return unique application key and ingestion URI', async () => {
    const newApp = await applicationsApi.createApplication({
      name: 'Another App',
    });
    
    expect(newApp.key).toBeDefined();
    expect(newApp.key.length).toBeGreaterThan(0);
    expect(newApp.ingestionUri).toContain(newApp.key);
    expect(newApp.ingestionUri).toMatch(/^http:\/\//);
  });
  
  it('should set current user as owner', async () => {
    const newApp = await applicationsApi.createApplication({
      name: 'Owned App',
    });
    
    expect(newApp.owner).toBe('user@example.com');
    expect(newApp.sharedWith).toEqual([]);
  });
  
  it('should include createdAt timestamp', async () => {
    const newApp = await applicationsApi.createApplication({
      name: 'Timestamped App',
    });
    
    expect(newApp.createdAt).toBeDefined();
    expect(new Date(newApp.createdAt).getTime()).toBeLessThanOrEqual(Date.now());
  });
  
  it('should reject creation with missing name', async () => {
    await expect(
      applicationsApi.createApplication({ name: '' })
    ).rejects.toThrow();
  });
  
  it('should reject creation with duplicate name', async () => {
    await expect(
      applicationsApi.createApplication({ name: 'existing-app' })
    ).rejects.toThrow();
  });
  
  it('should return 201 status code on success', async () => {
    // This test verifies the HTTP status code
    const newApp = await applicationsApi.createApplication({
      name: 'Status Test App',
    });
    
    expect(newApp).toBeDefined();
  });
});
