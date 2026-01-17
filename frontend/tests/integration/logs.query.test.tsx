/**
 * Contract Test: GET /api/applications/{id}/logs
 * 
 * Validates the logs query endpoint contract according to the API specification.
 * Tests retrieval of logs with proper pagination, filtering, and structure.
 * 
 * Test ID: T109
 * User Story: US3 - Log Viewing and Filtering
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete (per Constitution Section III)
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { logsApi } from '@/features/logs/api/logsApi';
import type { LogEntry } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';
const TEST_APP_ID = 'app-123';

const mockLogs: LogEntry[] = [
  {
    id: 'log-1',
    timestamp: '2024-01-01T12:00:00Z',
    level: 'error',
    message: 'Database connection failed',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'DatabaseService',
    metadata: {
      errorCode: 'DB_CONN_FAILED',
      stackTrace: 'at DatabaseService.connect...',
    },
  },
  {
    id: 'log-2',
    timestamp: '2024-01-01T12:01:00Z',
    level: 'warn',
    message: 'High memory usage detected',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'MemoryMonitor',
    metadata: {
      usagePercent: 85,
    },
  },
  {
    id: 'log-3',
    timestamp: '2024-01-01T12:02:00Z',
    level: 'info',
    message: 'User login successful',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'AuthService',
    metadata: {
      userId: 'user-456',
      mdc: {
        requestId: 'req-789',
        sessionId: 'sess-012',
      },
    },
  },
];

const handlers = [
  http.get(`${API_BASE_URL}/applications/:appId/logs`, ({ params, request }) => {
    const { appId } = params;
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    
    if (appId !== TEST_APP_ID) {
      return new HttpResponse(null, { status: 404 });
    }
    
    // Sort logs in reverse chronological order (newest first)
    const sortedLogs = [...mockLogs].sort((a, b) => 
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
    
    const start = page * size;
    const end = start + size;
    const paginatedLogs = sortedLogs.slice(start, end);
    
    return HttpResponse.json({
      paginatedLogs: {
        content: paginatedLogs,
        totalElements: mockLogs.length,
        totalPages: Math.ceil(mockLogs.length / size),
        number: page,
        size: size,
        first: page === 0,
        last: end >= mockLogs.length,
      },
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('GET /api/applications/{id}/logs - Contract Tests', () => {
  it('should return paginated logs for an application', async () => {
    const response = await logsApi.getLogs(TEST_APP_ID, { page: 1, limit: 20 });
    
    expect(response).toBeDefined();
    expect(response.data).toBeInstanceOf(Array);
    expect(response.total).toBe(mockLogs.length);
    expect(response.page).toBe(1);
    expect(response.limit).toBe(20);
  });

  it('should return logs with correct structure', async () => {
    const response = await logsApi.getLogs(TEST_APP_ID, { page: 1, limit: 20 });
    
    const log = response.data[0];
    expect(log).toBeDefined();
    expect(log).toHaveProperty('id');
    expect(log).toHaveProperty('timestamp');
    expect(log).toHaveProperty('level');
    expect(log).toHaveProperty('message');
    expect(log).toHaveProperty('applicationId');
    expect(log).toHaveProperty('source');
  });

  it('should return logs in reverse chronological order', async () => {
    const response = await logsApi.getLogs(TEST_APP_ID, { page: 1, limit: 20 });
    
    const timestamps = response.data.map(log => new Date(log.timestamp).getTime());
    const sortedTimestamps = [...timestamps].sort((a, b) => b - a);
    
    expect(timestamps).toEqual(sortedTimestamps);
  });

  it('should handle pagination correctly', async () => {
    const page1 = await logsApi.getLogs(TEST_APP_ID, { page: 1, limit: 2 });
    const page2 = await logsApi.getLogs(TEST_APP_ID, { page: 2, limit: 2 });
    
    expect(page1.data.length).toBeLessThanOrEqual(2);
    expect(page2.data.length).toBeLessThanOrEqual(2);
    expect(page1.data[0].id).not.toBe(page2.data[0]?.id);
  });

  it('should return 404 for non-existent application', async () => {
    server.use(
      http.get(`${API_BASE_URL}/applications/:appId/logs`, () => {
        return new HttpResponse(null, { status: 404 });
      })
    );
    
    await expect(logsApi.getLogs('non-existent-app', { page: 1, limit: 20 }))
      .rejects.toThrow();
  });

  it('should include metadata when present', async () => {
    const response = await logsApi.getLogs(TEST_APP_ID, { page: 1, limit: 20 });
    
    const logWithMetadata = response.data.find(log => log.metadata);
    expect(logWithMetadata).toBeDefined();
    expect(logWithMetadata?.metadata).toBeTypeOf('object');
  });

  it('should handle empty results', async () => {
    server.use(
      http.get(`${API_BASE_URL}/applications/:appId/logs`, () => {
        return HttpResponse.json({
          paginatedLogs: {
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: 0,
            size: 20,
            first: true,
            last: true,
          },
        });
      })
    );
    
    const response = await logsApi.getLogs(TEST_APP_ID, { page: 1, limit: 20 });
    
    expect(response.data).toEqual([]);
    expect(response.total).toBe(0);
  });
});
