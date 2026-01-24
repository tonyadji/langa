/**
 * Integration Test: Log Filtering
 * 
 * Tests the complete log filtering workflow including level, time range, and keyword filters.
 * Validates that filters work individually and in combination.
 * 
 * Test ID: T110
 * User Story: US3 - Log Viewing and Filtering
 * 
 * ⚠️ These tests MUST FAIL until implementation is complete (per Constitution Section III)
 */

import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { logsApi } from '@/features/logs/api/logsApi';
import type { LogEntry, LogLevel } from '@/types';

const API_BASE_URL = 'http://localhost:8080/api';
const TEST_APP_ID = 'app-123';

const allMockLogs: LogEntry[] = [
  {
    id: 'log-1',
    timestamp: '2024-01-01T10:00:00Z',
    level: 'error',
    message: 'Database connection failed',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'DatabaseService',
  },
  {
    id: 'log-2',
    timestamp: '2024-01-01T11:00:00Z',
    level: 'warn',
    message: 'High memory usage detected',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'MemoryMonitor',
  },
  {
    id: 'log-3',
    timestamp: '2024-01-01T12:00:00Z',
    level: 'info',
    message: 'User login successful',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'AuthService',
  },
  {
    id: 'log-4',
    timestamp: '2024-01-01T13:00:00Z',
    level: 'error',
    message: 'Failed to process payment',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'PaymentService',
  },
  {
    id: 'log-5',
    timestamp: '2024-01-02T10:00:00Z',
    level: 'debug',
    message: 'Cache hit for user data',
    applicationId: TEST_APP_ID,
    applicationName: 'Test App',
    source: 'CacheService',
  },
];

const handlers = [
  http.get(`${API_BASE_URL}/applications/:appId/logs`, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0');
    const size = parseInt(url.searchParams.get('size') || '20');
    const levelFilter = url.searchParams.get('level');
    const keyword = url.searchParams.get('keyword');
    const startDate = url.searchParams.get('startDate');
    const endDate = url.searchParams.get('endDate');
    
    let filteredLogs = [...allMockLogs];
    
    // Apply level filter
    if (levelFilter) {
      const levels = levelFilter.split(',').map(l => l.toLowerCase() as LogLevel);
      filteredLogs = filteredLogs.filter(log => levels.includes(log.level));
    }
    
    // Apply keyword filter (search in message and source)
    if (keyword) {
      const searchTerm = keyword.toLowerCase();
      filteredLogs = filteredLogs.filter(log => 
        log.message.toLowerCase().includes(searchTerm) ||
        log.source.toLowerCase().includes(searchTerm)
      );
    }
    
    // Apply time range filter
    if (startDate) {
      const start = new Date(startDate).getTime();
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp).getTime() >= start);
    }
    
    if (endDate) {
      const end = new Date(endDate).getTime();
      filteredLogs = filteredLogs.filter(log => new Date(log.timestamp).getTime() <= end);
    }
    
    const start = page * size;
    const end = start + size;
    const paginatedLogs = filteredLogs.slice(start, end);
    
    return HttpResponse.json({
      paginatedLogs: {
        content: paginatedLogs,
        totalElements: filteredLogs.length,
        totalPages: Math.ceil(filteredLogs.length / size),
        number: page,
        size: size,
        first: page === 0,
        last: end >= filteredLogs.length,
      },
    });
  }),
];

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('Log Filtering - Integration Tests', () => {
  describe('Level Filtering', () => {
    it('should filter logs by single level', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        level: ['error'] 
      });
      
      expect(response.data.every(log => log.level === 'error')).toBe(true);
      expect(response.total).toBe(2); // 2 error logs
    });

    it('should filter logs by multiple levels', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        level: ['error', 'warn'] 
      });
      
      expect(response.data.every(log => log.level === 'error' || log.level === 'warn')).toBe(true);
      expect(response.total).toBe(3); // 2 error + 1 warn
    });

    it('should return all logs when no level filter applied', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20 
      });
      
      expect(response.total).toBe(allMockLogs.length);
    });
  });

  describe('Keyword Filtering', () => {
    it('should filter logs by keyword in message', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        search: 'failed' 
      });
      
      expect(response.data.every(log => 
        log.message.toLowerCase().includes('failed')
      )).toBe(true);
      expect(response.total).toBe(2); // 2 logs with "failed"
    });

    it('should filter logs by source name', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        search: 'service' 
      });
      
      expect(response.data.every(log => 
        log.source.toLowerCase().includes('service')
      )).toBe(true);
    });

    it('should be case-insensitive', async () => {
      const lowercase = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        search: 'database' 
      });
      
      const uppercase = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        search: 'DATABASE' 
      });
      
      expect(lowercase.total).toBe(uppercase.total);
    });
  });

  describe('Time Range Filtering', () => {
    it('should filter logs by start date', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        startDate: '2024-01-01T12:00:00Z' 
      });
      
      expect(response.data.every(log => 
        new Date(log.timestamp).getTime() >= new Date('2024-01-01T12:00:00Z').getTime()
      )).toBe(true);
      expect(response.total).toBe(3); // 3 logs after 12:00
    });

    it('should filter logs by end date', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        endDate: '2024-01-01T12:00:00Z' 
      });
      
      expect(response.data.every(log => 
        new Date(log.timestamp).getTime() <= new Date('2024-01-01T12:00:00Z').getTime()
      )).toBe(true);
      expect(response.total).toBe(3); // 3 logs before 12:00 (inclusive)
    });

    it('should filter logs by date range', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        startDate: '2024-01-01T11:00:00Z',
        endDate: '2024-01-01T13:00:00Z' 
      });
      
      expect(response.data.every(log => {
        const timestamp = new Date(log.timestamp).getTime();
        return timestamp >= new Date('2024-01-01T11:00:00Z').getTime() &&
               timestamp <= new Date('2024-01-01T13:00:00Z').getTime();
      })).toBe(true);
      expect(response.total).toBe(3); // 3 logs in range
    });
  });

  describe('Combined Filters', () => {
    it('should apply level and keyword filters together', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        level: ['error'],
        search: 'failed' 
      });
      
      expect(response.data.every(log => 
        log.level === 'error' && log.message.toLowerCase().includes('failed')
      )).toBe(true);
    });

    it('should apply all filters together', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        level: ['error', 'warn'],
        search: 'failed',
        startDate: '2024-01-01T10:00:00Z',
        endDate: '2024-01-01T14:00:00Z'
      });
      
      expect(response.data.every(log => {
        const matchesLevel = log.level === 'error' || log.level === 'warn';
        const matchesKeyword = log.message.toLowerCase().includes('failed');
        const timestamp = new Date(log.timestamp).getTime();
        const matchesTimeRange = timestamp >= new Date('2024-01-01T10:00:00Z').getTime() &&
                                timestamp <= new Date('2024-01-01T14:00:00Z').getTime();
        return matchesLevel && matchesKeyword && matchesTimeRange;
      })).toBe(true);
    });

    it('should return empty array when no logs match all filters', async () => {
      const response = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 20, 
        level: ['debug'],
        search: 'nonexistent',
        startDate: '2025-01-01T00:00:00Z'
      });
      
      expect(response.data).toEqual([]);
      expect(response.total).toBe(0);
    });
  });

  describe('Filter Persistence with Pagination', () => {
    it('should maintain filters across pages', async () => {
      const page1 = await logsApi.getLogs(TEST_APP_ID, { 
        page: 1, 
        limit: 1, 
        level: ['error'] 
      });
      
      const page2 = await logsApi.getLogs(TEST_APP_ID, { 
        page: 2, 
        limit: 1, 
        level: ['error'] 
      });
      
      expect(page1.data[0].level).toBe('error');
      expect(page2.data[0].level).toBe('error');
      expect(page1.total).toBe(page2.total);
    });
  });
});
