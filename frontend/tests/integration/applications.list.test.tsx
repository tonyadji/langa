/**
 * Contract Test: GET /api/applications
 * 
 * Validates the application list endpoint contract according to the API specification.
 * Tests retrieval of owned and shared applications with proper pagination.
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { applicationsApi } from '@/features/applications/api/applicationsApi';
import type { Application } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

const mockApplications: Application[] = [
  {
    id: 'app-1',
    name: 'My First App',
    key: 'my-first-app-key-abc',
    accountKey: 'account-key-xyz',
    ingestionUri: 'http://localhost:8080/ingest/my-first-app-key-abc',
    owner: 'user@example.com',
    sharedWith: [],
    createdAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'app-2',
    name: 'Shared App',
    key: 'shared-app-key-def',
    accountKey: 'account-key-xyz',
    ingestionUri: 'http://localhost:8080/ingest/shared-app-key-def',
    owner: 'other@example.com',
    sharedWith: ['user@example.com'],
    createdAt: '2024-01-02T00:00:00Z',
  },
  {
    id: 'app-3',
    name: 'Team App',
    key: 'team-app-key-ghi',
    accountKey: 'account-key-xyz',
    ingestionUri: 'http://localhost:8080/ingest/team-app-key-ghi',
    owner: 'user@example.com',
    sharedWith: ['team:engineering'],
    createdAt: '2024-01-03T00:00:00Z',
  },
];

const handlers = [
  http.get(`${API_BASE_URL}/applications`, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '1');
    const limit = parseInt(url.searchParams.get('limit') || '10');
    
    const start = (page - 1) * limit;
    const end = start + limit;
    const paginatedApps = mockApplications.slice(start, end);
    
    return HttpResponse.json({
      applications: paginatedApps,
      total: mockApplications.length,
      page,
      limit,
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('GET /api/applications - Contract Tests', () => {
  it('should return list of applications', async () => {
    const response = await applicationsApi.getApplications({});
    
    expect(response).toBeDefined();
    expect(response.applications).toBeInstanceOf(Array);
    expect(response.applications.length).toBe(3);
  });
  
  it('should include both owned and shared applications', async () => {
    const response = await applicationsApi.getApplications({});
    
    const ownedApp = response.applications.find(app => app.id === 'app-1');
    const sharedApp = response.applications.find(app => app.id === 'app-2');
    
    expect(ownedApp).toBeDefined();
    expect(ownedApp?.owner).toBe('user@example.com');
    expect(sharedApp).toBeDefined();
    expect(sharedApp?.sharedWith).toContain('user@example.com');
  });
  
  it('should return pagination metadata', async () => {
    const response = await applicationsApi.getApplications({ page: 1, limit: 10 });
    
    expect(response.total).toBe(3);
    expect(response.page).toBe(1);
    expect(response.limit).toBe(10);
  });
  
  it('should support pagination', async () => {
    const response = await applicationsApi.getApplications({ page: 1, limit: 2 });
    
    expect(response.applications.length).toBe(2);
    expect(response.total).toBe(3);
  });
  
  it('should return application with all required fields', async () => {
    const response = await applicationsApi.getApplications({});
    const app = response.applications[0];
    
    expect(app.id).toBeDefined();
    expect(app.name).toBeDefined();
    expect(app.key).toBeDefined();
    expect(app.accountKey).toBeDefined();
    expect(app.ingestionUri).toBeDefined();
    expect(app.owner).toBeDefined();
    expect(app.sharedWith).toBeInstanceOf(Array);
    expect(app.createdAt).toBeDefined();
  });
  
  it('should return empty array when no applications exist', async () => {
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
    
    const response = await applicationsApi.getApplications({});
    
    expect(response.applications).toEqual([]);
    expect(response.total).toBe(0);
  });
  
  it('should include sharedWith information', async () => {
    const response = await applicationsApi.getApplications({});
    const teamApp = response.applications.find(app => app.id === 'app-3');
    
    expect(teamApp?.sharedWith).toContain('team:engineering');
  });
});
