/**
 * Integration Test: Metrics Query
 * Test ID: T136 | US4 - Metrics Visualization
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { metricsApi } from '@/features/metrics/api/metricsApi';
import type { MetricsResponse } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

const mockMetricsResponse: MetricsResponse = {
  appName: 'Test Application',
  paginatedMetrics: {
    content: [
      {
        name: 'api.request',
        durationMillis: 150,
        status: 'SUCCESS',
        timestamp: 1704729600000,
        uri: '/api/users',
        httpMethod: 'GET',
        httpStatus: 200,
      },
      {
        name: 'api.request',
        durationMillis: 250,
        status: 'SUCCESS',
        timestamp: 1704729660000,
        uri: '/api/products',
        httpMethod: 'POST',
        httpStatus: 201,
      },
    ],
    totalElements: 25,
    totalPages: 2,
    page: 0,
    size: 20,
  },
};

const handlers = [
  http.get(`${API_BASE_URL}/applications/:appId/metrics`, ({ params, request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');

    return HttpResponse.json({
      ...mockMetricsResponse,
      paginatedMetrics: {
        ...mockMetricsResponse.paginatedMetrics,
        page,
        size,
      },
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('GET /api/applications/{id}/metrics - Integration Tests', () => {
  it('should fetch metrics from API', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app-id');
    expect(response).toEqual(mockMetricsResponse);
  });

  it('should return metrics with correct structure', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app-id');
    expect(response).toHaveProperty('appName');
    expect(response).toHaveProperty('paginatedMetrics');
  });

  it('should handle pagination', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app-id', { page: 1, size: 10 });
    expect(response.paginatedMetrics.page).toBe(1);
    expect(response.paginatedMetrics.size).toBe(10);
  });
});
