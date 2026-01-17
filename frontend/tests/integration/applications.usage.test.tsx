/**
 * T206: Contract test for GET /api/applications/{id}/usage
 * 
 * Tests the API contract for retrieving application storage usage statistics.
 * Validates response structure, data types, and required fields per FR-031.
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

interface ApplicationUsage {
  id: string;
  appKey: string;
  totalLogBytes: number;
  totalMetricBytes: number;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

describe('GET /api/applications/{id}/usage - Contract Tests', () => {
  const mockUsageData: ApplicationUsage = {
    id: 'usage-1',
    appKey: 'app-123',
    totalLogBytes: 1048576, // 1 MB
    totalMetricBytes: 524288, // 512 KB
  };

  const handlers = [
    http.get(`${API_BASE_URL}/api/applications/:id/usage`, ({ params }) => {
      const { id } = params;

      if (id === 'app-123') {
        return HttpResponse.json(mockUsageData, { status: 200 });
      }

      if (id === 'app-404') {
        return HttpResponse.json(
          { error: 'Application not found' },
          { status: 404 }
        );
      }

      if (id === 'app-403') {
        return HttpResponse.json(
          { error: 'Forbidden - only application owners can view usage statistics' },
          { status: 403 }
        );
      }

      return HttpResponse.json(mockUsageData, { status: 200 });
    }),
  ];

  const server = setupServer(...handlers);

  beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
  afterEach(() => server.resetHandlers());
  afterAll(() => server.close());

  it('should return usage statistics with correct structure', async () => {
    const response = await fetch(`${API_BASE_URL}/api/applications/app-123/usage`);
    const data: ApplicationUsage = await response.json();

    expect(response.status).toBe(200);
    expect(data).toHaveProperty('id');
    expect(data).toHaveProperty('appKey');
    expect(data).toHaveProperty('totalLogBytes');
    expect(data).toHaveProperty('totalMetricBytes');
  });

  it('should return numeric values for byte counts', async () => {
    const response = await fetch(`${API_BASE_URL}/api/applications/app-123/usage`);
    const data: ApplicationUsage = await response.json();

    expect(typeof data.totalLogBytes).toBe('number');
    expect(typeof data.totalMetricBytes).toBe('number');
    expect(data.totalLogBytes).toBeGreaterThanOrEqual(0);
    expect(data.totalMetricBytes).toBeGreaterThanOrEqual(0);
  });

  it('should return correct appKey matching the application', async () => {
    const response = await fetch(`${API_BASE_URL}/api/applications/app-123/usage`);
    const data: ApplicationUsage = await response.json();

    expect(data.appKey).toBe('app-123');
  });

  it('should return 404 for non-existent application', async () => {
    const response = await fetch(`${API_BASE_URL}/api/applications/app-404/usage`);
    
    expect(response.status).toBe(404);
  });

  it('should return 403 for non-owner access (FR-023)', async () => {
    const response = await fetch(`${API_BASE_URL}/api/applications/app-403/usage`);
    
    expect(response.status).toBe(403);
    
    const error = await response.json();
    expect(error.error).toContain('Forbidden');
  });

  it('should handle large byte values correctly', async () => {
    const largeUsage: ApplicationUsage = {
      id: 'usage-2',
      appKey: 'app-large',
      totalLogBytes: 10737418240, // 10 GB
      totalMetricBytes: 5368709120, // 5 GB
    };

    server.use(
      http.get(`${API_BASE_URL}/api/applications/app-large/usage`, () => {
        return HttpResponse.json(largeUsage, { status: 200 });
      })
    );

    const response = await fetch(`${API_BASE_URL}/api/applications/app-large/usage`);
    const data: ApplicationUsage = await response.json();

    expect(data.totalLogBytes).toBe(10737418240);
    expect(data.totalMetricBytes).toBe(5368709120);
  });

  it('should return zero bytes for applications with no data', async () => {
    const zeroUsage: ApplicationUsage = {
      id: 'usage-3',
      appKey: 'app-new',
      totalLogBytes: 0,
      totalMetricBytes: 0,
    };

    server.use(
      http.get(`${API_BASE_URL}/api/applications/app-new/usage`, () => {
        return HttpResponse.json(zeroUsage, { status: 200 });
      })
    );

    const response = await fetch(`${API_BASE_URL}/api/applications/app-new/usage`);
    const data: ApplicationUsage = await response.json();

    expect(data.totalLogBytes).toBe(0);
    expect(data.totalMetricBytes).toBe(0);
  });
});
