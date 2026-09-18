/**
 * Integration Test: Metrics Filtering
 * Test ID: T137 | US4 - Metrics Visualization
 * 
 * Tests the filtering capabilities of the metrics API.
 */

import { describe, it, expect, beforeAll, afterAll, afterEach } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { metricsApi } from '@/features/metrics/api/metricsApi';
import type { MetricsResponse, Metric } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';

const allMetrics: Metric[] = [
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
  {
    name: 'api.error',
    durationMillis: 500,
    status: 'FAILURE',
    timestamp: 1704729720000,
    uri: '/api/orders',
    httpMethod: 'DELETE',
    httpStatus: 500,
  },
  {
    name: 'database.query',
    durationMillis: 75,
    status: 'SUCCESS',
    timestamp: 1704729780000,
    uri: '/api/database',
    httpMethod: 'GET',
    httpStatus: 200,
  },
];

const handlers = [
  http.get(`${API_BASE_URL}/applications/:appId/metrics`, ({ request }) => {
    const url = new URL(request.url);
    
    let filtered = [...allMetrics];

    // Filter by name
    const name = url.searchParams.get('name');
    if (name) {
      filtered = filtered.filter(m => m.name.includes(name));
    }

    // Filter by status
    const status = url.searchParams.get('status');
    if (status) {
      filtered = filtered.filter(m => m.status === status);
    }

    // Filter by URI
    const uri = url.searchParams.get('uri');
    if (uri) {
      filtered = filtered.filter(m => m.uri.includes(uri));
    }

    // Filter by HTTP method
    const httpMethod = url.searchParams.get('httpMethod');
    if (httpMethod) {
      filtered = filtered.filter(m => m.httpMethod === httpMethod);
    }

    // Filter by HTTP status
    const httpStatus = url.searchParams.get('httpStatus');
    if (httpStatus) {
      filtered = filtered.filter(m => m.httpStatus === parseInt(httpStatus));
    }

    // Filter by duration
    const durationLessThan = url.searchParams.get('durationLessThan');
    if (durationLessThan) {
      filtered = filtered.filter(m => m.durationMillis < parseInt(durationLessThan));
    }

    const durationGreaterThan = url.searchParams.get('durationGreaterThan');
    if (durationGreaterThan) {
      filtered = filtered.filter(m => m.durationMillis > parseInt(durationGreaterThan));
    }

    const response: MetricsResponse = {
      appName: 'Test Application',
      paginatedMetrics: {
        content: filtered,
        totalElements: filtered.length,
        totalPages: 1,
        page: 0,
        size: 20,
      },
    };

    return HttpResponse.json(response);
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('Metrics Filtering - Integration Tests', () => {
  it('should filter by name', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { name: 'api' });
    
    expect(response.paginatedMetrics.content).toHaveLength(3);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.name).toContain('api');
    });
  });

  it('should filter by status', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { status: 'SUCCESS' });
    
    expect(response.paginatedMetrics.content).toHaveLength(3);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.status).toBe('SUCCESS');
    });
  });

  it('should filter by URI', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { uri: '/api/users' });
    
    expect(response.paginatedMetrics.content).toHaveLength(1);
    expect(response.paginatedMetrics.content[0].uri).toBe('/api/users');
  });

  it('should filter by HTTP method', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { httpMethod: 'GET' });
    
    expect(response.paginatedMetrics.content).toHaveLength(2);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.httpMethod).toBe('GET');
    });
  });

  it('should filter by HTTP status', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { httpStatus: 200 });
    
    expect(response.paginatedMetrics.content).toHaveLength(2);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.httpStatus).toBe(200);
    });
  });

  it('should filter by duration less than', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { durationLessThan: 200 });
    
    expect(response.paginatedMetrics.content).toHaveLength(2);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.durationMillis).toBeLessThan(200);
    });
  });

  it('should filter by duration greater than', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', { durationGreaterThan: 200 });
    
    expect(response.paginatedMetrics.content).toHaveLength(2);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.durationMillis).toBeGreaterThan(200);
    });
  });

  it('should apply multiple filters simultaneously', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', {
      name: 'api',
      status: 'SUCCESS',
      httpMethod: 'GET',
    });
    
    expect(response.paginatedMetrics.content).toHaveLength(1);
    const metric = response.paginatedMetrics.content[0];
    expect(metric.name).toContain('api');
    expect(metric.status).toBe('SUCCESS');
    expect(metric.httpMethod).toBe('GET');
  });

  it('should return empty array when no metrics match filters', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', {
      name: 'nonexistent',
    });
    
    expect(response.paginatedMetrics.content).toHaveLength(0);
  });

  it('should handle complex filter combinations', async () => {
    const response = await metricsApi.getApplicationMetrics('test-app', {
      status: 'SUCCESS',
      durationLessThan: 200,
      httpStatus: 200,
    });
    
    expect(response.paginatedMetrics.content).toHaveLength(2);
    response.paginatedMetrics.content.forEach(m => {
      expect(m.status).toBe('SUCCESS');
      expect(m.durationMillis).toBeLessThan(200);
      expect(m.httpStatus).toBe(200);
    });
  });
});
